package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;
import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.infra.email.Carteiro;

public class EncerradorDeLeilaoTest {
	
	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAntes() {
		Calendar dataAntiga = Calendar.getInstance();
		dataAntiga.set(1998, 11, 22);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(dataAntiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Celular").naData(dataAntiga).constroi();
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		//Criando mock
		RepositorioDeLeiloes dao = mock(RepositorioDeLeiloes.class);
		Carteiro carteiro = mock(Carteiro.class);
		//Sempre que o metodo correntes for invocado, retorne esta lista
		when(dao.correntes()).thenReturn(leiloesAntigos);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(dao, carteiro);
		encerrador.encerra();
		
		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());
	}
	
	@Test
	public void deveAtualizarLeiloesEncerrados() {
		Calendar dataAntiga = Calendar.getInstance();
		dataAntiga.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(dataAntiga).constroi();
		
		RepositorioDeLeiloes dao = mock(RepositorioDeLeiloes.class);
		Carteiro carteiro = mock(Carteiro.class);
		when(dao.correntes()).thenReturn(Arrays.asList(leilao1));
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(dao, carteiro);
		encerrador.encerra();
		
		verify(dao, times(1)).atualiza(leilao1);
	}
	
	@Test
	public void deveContinuarAExecucaoMesmoQuandoDaoFalha() {
		Calendar dataAntiga = Calendar.getInstance();
		dataAntiga.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(dataAntiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(dataAntiga).constroi();
		
		RepositorioDeLeiloes dao = mock(RepositorioDeLeiloes.class);
		Carteiro carteiro = mock(Carteiro.class);
		
		when(dao.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
		doThrow(new RuntimeException()).when(dao).atualiza(leilao1);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(dao, carteiro);
		encerrador.encerra();
		
		verify(dao).atualiza(leilao2);
		verify(carteiro).envia(leilao2);
		verify(carteiro, times(0)).envia(leilao1);
	}
}
