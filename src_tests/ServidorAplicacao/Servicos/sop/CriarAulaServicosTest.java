/*
 * CriarAulaServicosTest.java
 * JUnit based test
 *
 * Created on 26 de Outubro de 2002, 15:51
 */

package ServidorAplicacao.Servicos.sop;

/**
 *
 * @author tfc130
 */
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import DataBeans.InfoExecutionCourse;
import DataBeans.InfoExecutionPeriod;
import DataBeans.InfoExecutionYear;
import DataBeans.InfoLesson;
import DataBeans.InfoLessonServiceResult;
import DataBeans.InfoRoom;
import ServidorAplicacao.GestorServicos;
import ServidorAplicacao.Servico.UserView;
import ServidorAplicacao.Servicos.TestCaseCreateServices;
import Util.DiaSemana;
import Util.TipoAula;
import Util.TipoSala;

public class CriarAulaServicosTest extends TestCaseCreateServices {

	public CriarAulaServicosTest(java.lang.String testName) {
		super(testName);
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CriarAulaServicosTest.class);

		return suite;
	}

	protected void setUp() {
		super.setUp();
	}

	protected void tearDown() {
		super.tearDown();
	}

	protected HashMap getArgumentListOfServiceToBeTestedUnsuccessfuly() {
		
		HashMap hashMap = new HashMap();

		// write existing lesson with complete match
		InfoRoom infoSala = new InfoRoom("Ga1", "Pavilhao Central", new Integer(0), new TipoSala(1), new Integer(100), new Integer(50));
		InfoExecutionCourse infoDisciplinaExecucao = new InfoExecutionCourse( "Trabalho Final de Curso I", "TFCI", "programa1",
																				new Double(0), new Double(0), new Double(0),
																				new Double(0), new InfoExecutionPeriod("2� Semestre", new InfoExecutionYear("2002/2003")));
/*
		Object argsCriarAula[] = new Object[1];
		Calendar inicio = Calendar.getInstance();
		Calendar fim = Calendar.getInstance();
		inicio.set(Calendar.HOUR_OF_DAY, 8);
		inicio.set(Calendar.MINUTE, 0);
		inicio.set(Calendar.SECOND, 0);
		fim.set(Calendar.HOUR_OF_DAY, 9);
		fim.set(Calendar.MINUTE, 30);
		fim.set(Calendar.SECOND, 0);
		argsCriarAula[0] = new InfoLesson(new DiaSemana(DiaSemana.SEGUNDA_FEIRA), inicio, fim, new TipoAula(1), infoSala, infoDisciplinaExecucao);

		List argsInList = Arrays.asList(argsCriarAula);
		hashMap.put("write new existing lesson with complete match", argsInList);
*/
		Object argsCriarAula2[] = new Object[1];
		Calendar inicio2 = Calendar.getInstance();
		Calendar fim2 = Calendar.getInstance();
		inicio2.set(Calendar.HOUR_OF_DAY, 8);
		inicio2.set(Calendar.MINUTE, 30);
		inicio2.set(Calendar.SECOND, 0);
		fim2.set(Calendar.HOUR_OF_DAY, 9);
		fim2.set(Calendar.MINUTE, 0);
		fim2.set(Calendar.SECOND, 0);
		argsCriarAula2[0] = new InfoLesson(new DiaSemana(DiaSemana.SEGUNDA_FEIRA), inicio2, fim2, new TipoAula(1), infoSala, infoDisciplinaExecucao);

		List argsInList2 = Arrays.asList(argsCriarAula2);
		hashMap.put("write new existing lesson with intercepting match", argsInList2);

		return null; //hashMap;
	}

	protected Object[] getArgumentsOfServiceToBeTestedSuccessfuly() {
		return null;
	}

	protected Object[] getArgumentsOfServiceToBeTestedUnsuccessfuly() {
		return null;
	}

	protected String getNameOfServiceToBeTested() {
		return "CriarAula";
	}









/*
	// write new existing lesson with complete match
	public void testCreateExistingLessonCompleteMatch() {
		InfoRoom infoSala =
			new InfoRoom(
				"Ga1",
				"Pavilhao Central",
				new Integer(0),
				new TipoSala(1),
				new Integer(100),
				new Integer(50));
		InfoExecutionCourse infoDisciplinaExecucao =
			new InfoExecutionCourse(
				"Trabalho Final de Curso I",
				"TFCI",
				"programa1",
				new Double(0),
				new Double(0),
				new Double(0),
				new Double(0), 
				new InfoExecutionPeriod(
					"2� Semestre",
					new InfoExecutionYear("2002/2003")));
		Object argsCriarAula[] = new Object[1];
		Calendar inicio = Calendar.getInstance();
		Calendar fim = Calendar.getInstance();
		inicio.set(Calendar.HOUR_OF_DAY, 8);
		inicio.set(Calendar.MINUTE, 0);
		inicio.set(Calendar.SECOND, 0);
		fim.set(Calendar.HOUR_OF_DAY, 9);
		fim.set(Calendar.MINUTE, 30);
		fim.set(Calendar.SECOND, 0);
		argsCriarAula[0] =
			new InfoLesson(
				new DiaSemana(2),
				inicio,
				fim,
				new TipoAula(1),
				infoSala,
				infoDisciplinaExecucao);
		GestorServicos serviceManager = GestorServicos.manager();
		Object result = null;
		HashSet privileges = new HashSet();
		privileges.add("CriarAula");
		privileges.add("LerAulasDeDisciplinaExecucaoETipo");
		UserView userView = new UserView("user", privileges);
		try {
			result = serviceManager.executar(userView, "CriarAula", argsCriarAula);
			fail("testCreateExistingLessonCompleteMatch");
		
		} catch (Exception ex) {
			// all is ok
			

		}
	}
*/
//	write new existing lesson with intercepting match
	 public void testCreateExistingLessonInterceptingMatch() {
		 InfoRoom infoSala =
			 new InfoRoom(
				 "Ga1",
				 "Pavilhao Central",
				 new Integer(0),
				 new TipoSala(1),
				 new Integer(100),
				 new Integer(50));
		 InfoExecutionCourse infoDisciplinaExecucao =
			 new InfoExecutionCourse(
				 "Trabalho Final de Curso I",
				 "TFCI",
				 "programa1",
				 new Double(0),
				 new Double(0),
				 new Double(0),
				 new Double(0), 
				 new InfoExecutionPeriod(
					 "2� Semestre",
					 new InfoExecutionYear("2002/2003")));
		 Object argsCriarAula[] = new Object[1];
		 Calendar inicio = Calendar.getInstance();
		 Calendar fim = Calendar.getInstance();
		 inicio.set(Calendar.HOUR_OF_DAY, 8);
		 inicio.set(Calendar.MINUTE, 30);
		 inicio.set(Calendar.SECOND, 0);
		 fim.set(Calendar.HOUR_OF_DAY, 9);
		 fim.set(Calendar.MINUTE, 0);
		 fim.set(Calendar.SECOND, 0);
		 argsCriarAula[0] =
			 new InfoLesson(
				 new DiaSemana(2),
				 inicio,
				 fim,
				 new TipoAula(1),
				 infoSala,
				 infoDisciplinaExecucao);
		 GestorServicos serviceManager = GestorServicos.manager();
		 Object result = null;
		 HashSet privileges = new HashSet();
		 privileges.add("CriarAula");
		 privileges.add("LerAulasDeDisciplinaExecucaoETipo");
		 UserView userView = new UserView("user", privileges);
		 try {
			 result = serviceManager.executar(userView, "CriarAula", argsCriarAula);
			fail("testCreateExistingLessonInterceptingMatch");
		 } catch (Exception ex) {
			ex.printStackTrace();
		 }
	 }

	// write new non-existing aula
	public void testCreateNonExistingAula() {
		InfoRoom infoSala =
			new InfoRoom(
				"Ga1",
				"Pavilhao Central",
				new Integer(0),
				new TipoSala(1),
				new Integer(100),
				new Integer(50));
		InfoExecutionCourse infoDisciplinaExecucao =
			new InfoExecutionCourse(
				"Trabalho Final de Curso I",
				"TFCI",
				"programa1",
				new Double(0),
				new Double(0),
				new Double(0),
				new Double(0),
				new InfoExecutionPeriod(
					"2� Semestre",
					new InfoExecutionYear("2002/2003")));
		Object argsCriarAula[] = new Object[1];
		Calendar inicio = Calendar.getInstance();
		Calendar fim = Calendar.getInstance();
		inicio.set(Calendar.HOUR_OF_DAY, 10);
		inicio.set(Calendar.MINUTE, 0);
		inicio.set(Calendar.SECOND, 0);
		fim.set(Calendar.HOUR_OF_DAY, 12);
		fim.set(Calendar.MINUTE, 0);
		fim.set(Calendar.SECOND, 0);
		argsCriarAula[0] =
			new InfoLesson(
				new DiaSemana(1),
				inicio,
				fim,
				new TipoAula(1),
				infoSala,
				infoDisciplinaExecucao);
		GestorServicos serviceManager = GestorServicos.manager();
		Object result = null;
		HashSet privileges = new HashSet();
	    privileges.add("CriarAula");
		privileges.add("LerAulasDeDisciplinaExecucaoETipo");
		UserView userView = new UserView("user", privileges);
				try {
			result = serviceManager.executar(userView, "CriarAula", argsCriarAula);
			assertTrue(
				"testCreateNonExistingAula",
				((InfoLessonServiceResult) result).isSUCESS());
		} catch (Exception ex) {
			fail("testCreateNonExistingAula");
		}
	}
}
