<%@ page import="org.sakaiproject.user.tool.UserPrefsTool" %><%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%-- Core JSTL tag library --%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>

<%-- dummy variable to check for active Date --%>
<c:set var="dummy" value="${UserPrefsTool.dummy}" scope = "request"/>
<c:set var="localeAbvr" value="${UserPrefsTool.checkLocale}" scope = "request"/>

<f:view>
    <sakai:view_container title="#{msgs.prefs_title}">
        <sakai:stylesheet path="/css/prefs.css"/>
        <sakai:view_content>
            <h:form id="outOfOffice_form">
                <title>jQuery UI Datepicker - Default functionality</title>
                <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
                <link rel="stylesheet" href="/resources/demos/style.css">
                <script src="https://code.jquery.com/jquery-1.12.4.js"></script>
                <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
                <script src="css/jquery-1.9.1.js"></script>
                <script src="css/jquery.ui.core.js"></script>
                <script src="css/jquery.ui.widget.js"></script>
                <script src="css/jquery.ui.datepicker.js"></script>
                <script type="text/javascript" src="/sakai-user-tool-prefs/js/prefs.js">// </script>
                <script type="text/javascript" src="/library/js/spinner.js"></script>
                <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/prefs.css">



                <%-- to use localeAbvr with datepicker --%>
                <div id="localeValue" style="visibility: hidden">${localeAbvr}</div>


                <script type="text/javascript">
                    let valueLanguage = $('#localeValue').html();
                    $(document).ready(function(){
                        $(".datepicker").datepicker({dateFormat: "mm/dd/yy"});
                        if(valueLanguage === "de"){
                            $(".datepicker").datepicker( "option", "dateFormat", "dd/mm/yy" );
                        }
                    });
                </script>


                <script type="text/javascript">
                    $(document).ready(function(){
                        $("#outOfOffice_form\\:notValid").fadeTo(3000,1).fadeOut(1000);
                    });
                </script>


                <c:set var="cTemplate" value = "outOfOffice" scope="session"/>

                <%@ include file="toolbar.jspf"%>



                <h3 style="display: inline-block;">
                    <h:outputText  value="#{msgs.outOfOfficeHeader}" />
                </h3>

                <p class="instruction"><h:outputText value="#{msgs.outOfOfficeConfig}"  /></p>
                <t:div  rendered="#{UserPrefsTool.isActive}">
                    <h:outputText id="activeDate"  styleClass="isActiveDate" value="#{msgs.outOfOfficeActive} #{UserPrefsTool.activeDate}" rendered="#{UserPrefsTool.isActive}"/>
                </t:div>

                <t:div  rendered="#{UserPrefsTool.dateNotValid}">
                    <h:outputText id="notValid"  styleClass="notValidDate" value="#{msgs.invalidDate}" />
                </t:div>


                <h:outputText value="#{msgs.outOfOfficeInputDate}"/>

                <h:inputText styleClass="datepicker"  value="#{UserPrefsTool.selectedDate}"></h:inputText>

                <div class="submit-buttons act">
                    <sakai:button_bar>
                        <sakai:button_bar_item action="#{UserPrefsTool.processActionOutOfOfficeSave}" value="#{msgs.outOfOfficeUpdate}" styleClass="active" rendered="#{UserPrefsTool.isActive}" />
                        <sakai:button_bar_item action="#{UserPrefsTool.processActionOutOfOfficeSave}" value="#{msgs.outOfOfficeActivate}" styleClass="active" rendered="#{!UserPrefsTool.isActive}" />
                        <sakai:button_bar_item action="#{UserPrefsTool.processActionOutOfOfficeDelete}" value="#{msgs.outOfOfficeDeactivate}" styleClass="active" disabled="#{!UserPrefsTool.isActive}" />
                    </sakai:button_bar>
                </div>
            </h:form>
        </sakai:view_content>
    </sakai:view_container>
</f:view>
