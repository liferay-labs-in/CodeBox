<%@page import="com.liferay.portal.kernel.module.configuration.ConfigurationException"%>
<%@page import="com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil"%>
<%@page import="com.liferay.gsindia.billdesk.configuration.BilldeskConfiguration"%>
<%@ include file="/init.jsp" %>

<portlet:resourceURL id="payNow" var="payNow" />

 <script type="module"
src="https://uat.billdesk.com/jssdk/v1/dist/billdesksdk/billdesksdk.esm.js"></script>
<script nomodule="" src="https://uat.billdesk.com/jssdk/v1/dist/billdesksdk.js"></script>
<link href="https://uat.billdesk.com/jssdk/v1/dist/billdesksdk/billdesksdk.css" rel="stylesheet">

<%

BilldeskConfiguration _billDeskConfiguration=null;
try {
	_billDeskConfiguration = ConfigurationProviderUtil.getSystemConfiguration(BilldeskConfiguration.class);
} catch (ConfigurationException e1) {
	e1.printStackTrace();
}

%>
<p>
	<b>Do Billdesk Payment </b>
</p>
<input type="hidden" id="namespace" value="<portlet:namespace/>">
<div class="col-wrapper">
<input pattern="^\d*(\.\d{0,2})?$"  value="" class="advance-amt-input" type="text" name="Amount" placeholder='enter amount'/>
<span class="reset-input"></span>
</div>
<div class="applicable">
<aui:button type="button"  onClick="payNow()" value="Pay Now" >								
</aui:button>
</div>

<script>
var payNowURL = "<%=payNow%>";


function payNow() {
	$("#full-page-loading").show();
	var namespace=$("#namespace").val();
	var bu="POWER";
	var amount=$(".advance-amt-input").val();
	var token="";
	var bdOrderId="";
	var formData = new FormData();
	var column1RelArray = [];
	formData.append("<portlet:namespace />BU" ,bu);
	formData.append("<portlet:namespace />AMOUNT" ,amount);
	column1RelArray.push({
    	"Document_No":"30989778888",
    	"Amount":amount
    });
	
	const OrderData = JSON.stringify(column1RelArray);
	var formData = new FormData();
	formData.append(namespace+"OrderData" , OrderData);
	console.log("Form Data "+OrderData);
	 $.ajax({
	        url : payNowURL,
	        processData : false,
			contentType : false,
			type : 'POST',
			data : formData,
	        success : function(data){

	        	if(data == "" || data==undefined){
	        	}else{
	    	        var jsonObject = JSON.parse(data);
	    	        token=jsonObject.token;
	    	        console.log(token);
	    	        bdOrderId=jsonObject.bdOrderId;
	    	        var flow_config = {
	    	        		merchantId: "PLSIND2UAT",
	    	        		bdOrderId: bdOrderId,
	    	        		authToken: token,
	    	        		childWindow: false,
	    	        		returnUrl: "https://<yourwebsiteURL>/group/guest/arresponse",
	    	        		retryCount: 3,
	    	        		//prefs :{"payment_categories": ["card", "emi"] }
	    	        		}

	    	        var responseHandler = function (txn) {
	    	        	 
	    	        	 }

	    	        var config = {
	    	        responseHandler: responseHandler,
	    	        merchantLogo: "",
	    	        flowConfig: flow_config,
	    	        flowType: "payments"
	    	        }
	    	        
	    	        window.loadBillDeskSdk(config);
	         
	        }
				$("#full-page-loading").hide();
	        	$("#confirm-os-model").modal("show");
	        },
	        failure : function(){
	        	alert("Failed");
	        },
			complete:function(){
				$("#full-page-loading").hide();
			}
	      });
}
</script>