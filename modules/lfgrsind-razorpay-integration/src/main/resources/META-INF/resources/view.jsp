<%@ include file="/init.jsp"%>
<%@page import="com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil"%>
<%@page import="com.liferay.gsindia.configuration.RazorpayConfiguration"%>

<portlet:resourceURL var="orderCreationUrl" id="orderCreationUrl">
	<portlet:param name="action" value="orderCreation" />
</portlet:resourceURL>
<portlet:resourceURL var="verifySigUrl" id="verifySigUrl">
	<portlet:param name="action" value="verifySig" />
</portlet:resourceURL>

<%
RazorpayConfiguration razorpayConfiguration = ConfigurationProviderUtil.getSystemConfiguration(RazorpayConfiguration.class);
String keyId= null;
keyId= razorpayConfiguration.keyId();
%>

<script src="https://checkout.razorpay.com/v1/checkout.js"></script>
<aui:script>

var imageUrl = '<%=request.getContextPath()%>';
var keyId = '<%=keyId %>';
//console.log("Key Id :"+keyId);

function payNow()
{
	AUI().use('aui-base','aui-io-request', function(A){
	Liferay.Session.extend();
	var amount = document.getElementById('numberField').value;
	 A.io.request('<%=orderCreationUrl%>',{
         dataType: 'json',
         method: 'POST',
         data: { 
        	 <portlet:namespace/>amount: amount*100
             },
         on: {
         success: function(data){ 
         	var data=this.get('responseData'); 
        	 $.each(data, function(key, value) {	
        		 if (value.status == "created") {
                		var options = {
                			    "key": keyId,
                				"amount": value.amount_due, // Amount is in currency subunits. Default currency is INR. Hence, 50000 refers to 50000 paise
                			    "currency": value.currency,
                			    "name": "Liferay",
                			    "description": "Razorpay Integration",
                			    "image": imageUrl+"/images/download.png",
                			    "order_id": value.orderId, // Pass the generated via Order API
                			    "handler": function (response){
                			    	console.log(response.razorpay_payment_id);
        							console.log(response.razorpay_order_id);
        							console.log(response.razorpay_signature)
                			    	verifySig(response,value.orderId); // Verify Payment Signature
                			     },                  			 
                			    "prefill":{
                			    	"name": "Prakash Kumar Sah",
                			    	"email": "prakash.kumar@liferay.com",
                			    	"contact":"9999999999"
                			    },
                			    "notes": {
                			        "address": "Liferay India"
                			    },
                			    "theme": {
                			        "color": "#003C71"
                			    }
                			};
                		var rzp1 = new Razorpay(options);
                		rzp1.open();
                		rzp1.on('payment.failed', function (response){ 
                			console.log(response.error.code);
         		        	console.log(response.error.description);
         		        	console.log(response.error.source);
         		        	console.log(response.error.step);
         		        	console.log(response.error.reason);
         		        	console.log(response.error.metadata.order_id);
         		        	console.log(response.error.metadata.payment_id);
         				});   
                		
                 	}
                	else{
                		alert("Failed to Create Order Id");
                	}

        		 
        	 })       
             }
         }
     	});
    });
}

function verifySig(response,orderId)
{
    console.log(response.razorpay_payment_id);
    AUI().use('aui-base','aui-io-request', function(A){
	
	 A.io.request('<%=verifySigUrl%>',{
         dataType: 'json',
         method: 'POST',
         data: { 
        	 <portlet:namespace/>paymentId: response.razorpay_payment_id,
        	 <portlet:namespace/>orderId: orderId,
        	 <portlet:namespace/>signature: response.razorpay_signature
             },
         on: {
         success: function(data){ 
         	var data=this.get('responseData'); 
        	 $.each(data, function(key, value) {
             	if(value.status == "true" && value.razorpayStatus == "captured"){
                	var paymentId = value.paymentId;
                    console.log("Sccessfull : "+paymentId);
                    alert("Payment Successfully Verified");
            	}
                else{
                	alert("Payment Verfication Failed");
    			}
               })
                   		
             }
		 }
		});
	});
}

</aui:script>


<p>
	<b><liferay-ui:message key="lfgrsindrazorpayintegration.caption" /></b>
</p>

<input type="number" id="numberField" placeholder="Enter a number">

<button onclick="payNow()">Submit</button>


