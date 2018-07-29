package br.ucdb.pos.engenhariasoftware.testesoftware.service;

import br.ucdb.pos.engenhariasoftware.testesoftware.controller.vo.LancamentoVO;
import br.ucdb.pos.engenhariasoftware.testesoftware.controller.vo.ResultadoVO;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.Lancamento;
import br.ucdb.pos.engenhariasoftware.testesoftware.modelo.TipoLancamento;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LancamentoServiceTest {

    @Mock
    private LancamentoService lancamentoService;

    @BeforeClass
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @DataProvider(name = "lancamentos")
    protected Object[][] getLancamentos(Method method) {
        List<Lancamento> lancamentosAleatorios = new ArrayList<>();
        for (int i = 0; i < ObterQuantidadeDeLancamentos(method.getName()); i++) {
            lancamentosAleatorios.add(new LancamentoBuilder().GerarLancamentorAleatorio().build());
        }
        return new Object[][]{
                new Object[]{lancamentosAleatorios}
        };
    }

    @Test(dataProvider = "lancamentos", groups = "lancamento10")
    public void BuscaAjaxTestLancamento10(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.buscaAjaxTest(lancamentos, 10);
    }

    @Test(dataProvider = "lancamentos", groups = "lancamento9")
    public void BuscaAjaxTestLancamento9(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.buscaAjaxTest(lancamentos, 9);
    }

    @Test(dataProvider = "lancamentos", groups = "lancamento3")
    public void BuscaAjaxTestLancamento3(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.buscaAjaxTest(lancamentos, 3);
    }

    @Test(dataProvider = "lancamentos", groups = "lancamento1")
    public void BuscaAjaxTestLancamento1(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.buscaAjaxTest(lancamentos, 1);
    }

    @Test(dataProvider = "lancamentos", groups = "lancamento0")
    public void BuscaAjaxTestLancamento0(List<Lancamento> lancamentos) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.buscaAjaxTest(lancamentos, 0);
    }

    private void buscaAjaxTest(List<Lancamento> lancamentos, long tamanhoEsperado) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        when(lancamentoService.somaValoresPorTipo(anyListOf(Lancamento.class), any(TipoLancamento.class))).thenCallRealMethod();
        when(lancamentoService.getTotalEntrada(anyListOf(Lancamento.class))).thenCallRealMethod();
        when(lancamentoService.getTotalSaida(anyListOf(Lancamento.class))).thenCallRealMethod();
        when(lancamentoService.conta(anyString())).thenReturn((long) lancamentos.size());
        when(lancamentoService.getResultadoVO(anyListOf(Lancamento.class), anyInt(), anyLong())).thenCallRealMethod();
        when(lancamentoService.busca(anyString())).thenReturn(lancamentos);
        when(lancamentoService.tamanhoPagina()).thenReturn(10);
        given(lancamentoService.buscaAjax(anyString())).willCallRealMethod();

        ValidarTotalDeEntradas(lancamentos);
        ValidarTotalDeSaidas(lancamentos);
        ValidarTamanhoDaLista(tamanhoEsperado);

        final ResultadoVO resultadoVO = lancamentoService.buscaAjax(anyString());
        Field[] campos = Lancamento.class.getDeclaredFields();
        for (LancamentoVO lancamentoVO : resultadoVO.getLancamentos()) {
            for (Field campo : campos) {
                String atributo = campo.getName();
                assertTrue(VerificarSeObjetoExisteNaClasse(lancamentoVO, atributo), "Atributo " + atributo + " não existe na Classe LancamentoVO.");
                String valorAtributo = ObterValorDoMetodo(lancamentoVO, atributo).toString();
                assertTrue(!valorAtributo.equals("") && valorAtributo != null, "Atributo " + atributo + " é null na Classe Lancamento VO.");
            }
        }

    }

    private int ObterQuantidadeDeLancamentos(String method){
        return Integer.parseInt(method.replaceAll("\\D+", ""));
    }

    private void ValidarTotalDeEntradas(List<Lancamento> lancamentos){
        BigDecimal valorTotalEntrada = lancamentoService.getTotalEntrada(lancamentos);
        BigDecimal valorTotalEntradaCorreto = this.ObterValorTotalPorTipo(lancamentos, TipoLancamento.ENTRADA);
        assertEquals(valorTotalEntrada, valorTotalEntradaCorreto, "Valor Correto de Saida: " + valorTotalEntradaCorreto.toString() + ", Valor Atual: " + valorTotalEntrada.toString());
    }

    private void ValidarTotalDeSaidas(List<Lancamento> lancamentos){
        BigDecimal valorTotalSaida =lancamentoService.getTotalSaida(lancamentos);
        BigDecimal valorTotalSaidaCorreto = this.ObterValorTotalPorTipo(lancamentos, TipoLancamento.SAIDA);
        assertEquals(valorTotalSaidaCorreto, valorTotalSaida, "Valor Correto de Entrada: " + valorTotalSaidaCorreto.toString() + ", Valor Atual: " + valorTotalSaida.toString());
    }

    private void ValidarTamanhoDaLista(long tamanhoCorretoDaLista){
        long tamanhoDaLista = lancamentoService.buscaAjax(anyString()).getTotalRegistros();
        assertEquals(tamanhoDaLista, tamanhoCorretoDaLista, "Tamanho Correto da Lista: " + tamanhoCorretoDaLista + " Tamanho da Lista Atual: " + tamanhoDaLista);
    }

    private BigDecimal ObterValorTotalPorTipo(List<Lancamento> lancamentos, TipoLancamento tipo){
        int tamanhoLista = lancamentos.size();

        if(tamanhoLista == 0)
            return BigDecimal.ZERO;
        else
            return lancamentos.stream()
                    .filter(t -> t.getTipoLancamento() == tipo)
                    .map(Lancamento::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Object ObterValorDoMetodo(Object object, String atributo) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            String getMethodtext = "get" + atributo.substring(0, 1).toUpperCase() + atributo.substring(1);
            Method getMethod = object.getClass().getMethod(getMethodtext, new Class[]{});
            return getMethod.invoke(object, new Object[]{});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean VerificarSeObjetoExisteNaClasse(Object object, String fieldName) {
        return Arrays.stream(object.getClass().getDeclaredFields()).anyMatch(f -> f.getName().equals(fieldName));
    }

    static class LancamentoBuilder{
        private Lancamento lancamento;

        LancamentoBuilder(){
            lancamento = new Lancamento();
        }

        LancamentoBuilder GerarLancamentorAleatorio(){
            Random rdm = new Random();
            return comId(rdm.nextInt())
                    .comData(GerarDiaAleatorio(), GerarMesAleatorio(), GerarAnoAleatorio())
                    .comTipo(rdm.nextBoolean() ? TipoLancamento.ENTRADA : TipoLancamento.SAIDA)
                    .comDescricao("Lançamento Aleatório " + rdm.nextInt() + " Trabalho Mokito")
                    .comValor(GerarValorAleatorio());
        }

        LancamentoBuilder comId(int id){
            lancamento.setId(id);
            return this;
        }

        LancamentoBuilder comData(int dia, int mes, int ano){
            Calendar calendario = Calendar.getInstance();
            calendario.set(ano, mes, dia);
            lancamento.setDataLancamento(calendario.getTime());
            return this;
        }

        private int GerarDiaAleatorio(){ return new Random().nextInt(27) + 1; }

        private int GerarMesAleatorio() { return new Random().nextInt(11) + 1; }

        private int GerarAnoAleatorio(){
            LocalDateTime horaAtual = LocalDateTime.now();
            return new Random().nextInt(horaAtual.getYear());
        }

        private double GerarValorAleatorio(){
            return new Random().nextInt(5000) / 100.0;
        }

        LancamentoBuilder comDescricao(String descricao){
            lancamento.setDescricao(descricao);
            return this;
        }

        LancamentoBuilder comValor(double valor){
            lancamento.setValor(BigDecimal.valueOf(valor));
            return this;
        }
        LancamentoBuilder comTipo(TipoLancamento tipo){
            lancamento.setTipoLancamento(tipo);
            return this;
        }

        Lancamento build(){
            return lancamento;
        }
    }
}
