<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ page import="ServidorApresentacao.Action.sop.utils.SessionConstants" %>

<ul class="treemenu">
	<li><a href="http://www.ist.utl.pt/pt/informacoes/">Informa��o</a></li>
  <li><a href="http://www.ist.utl.pt/pt/estrutura_interna/">Estrutura</a></li>
  <li><a href="http://www.ist.utl.pt/pt/servicos/">Servi�os</a></li>
  <li class="treenode"><a href="">Ensino</a>
		<ul class="expmenu">
			<li><html:link page="<%= "/showDegrees.do?method=nonMaster&executionPeriodOID=" + request.getAttribute(SessionConstants.EXECUTION_PERIOD_OID) %>" ><bean:message key="link.degree.nonMaster"/></html:link></li>
			<li><a href="http://www.ist.utl.pt/html/ensino/pos_grad.html">P�s-gradua��es</a></li>
			<li><html:link page="<%= "/showDegrees.do?method=master&executionPeriodOID=" + request.getAttribute(SessionConstants.EXECUTION_PERIOD_OID) %>" ><bean:message key="link.degree.master"/></html:link></li>
			<li><a href="http://www.ist.utl.pt/html/ensino/doutoramentos.html">Doutoramentos</a></li>
		</ul>
	</li>
  <li><a href="http://www.ist.utl.pt/pt/investigacao/">I &amp; D</a></li>
</ul>

<ul class="treemenu">
	<li><a href="http://gape.ist.utl.pt/acesso/">Ingressos</a></li>
  <li><a href="http://alumni.ist.utl.pt/">Sa�das</a></li>
</ul>

<ul class="treemenu">
	<li><a href="http://istpress.ist.utl.pt/">IST Press</a></li>
  <li><a href="http://www.ist.utl.pt/pt/ligacao_sociedade/">Sociedade &amp; IST</a></li>
  <li><a href="http://www.ist.utl.pt/pt/viver_ist/">Viver no IST</a></li>
  <li><a href="http://www.utl.pt/">Universidade</a></li>
</ul>