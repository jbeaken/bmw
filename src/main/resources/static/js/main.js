/*!
 * Main Javascript file for BMW website
 * Written by Jack 2013
 */
function detectmob() { 
	  if( navigator.userAgent.match(/Android/i)
		  || navigator.userAgent.match(/webOS/i)
		  //|| navigator.userAgent.match(/iPhone/i)
		  //|| navigator.userAgent.match(/iPad/i)
		  || navigator.userAgent.match(/iPod/i)
		  || navigator.userAgent.match(/BlackBerry/i)
		  || navigator.userAgent.match(/Windows Phone/i)
	  ){
	     return true;
	   }
	  else {
	     return false;
	   }
}	
function detectmobByScreenWidth() {
   if(window.innerWidth <= 800 && window.innerHeight <= 600) {
	   return true;
   } else {
	   return false;
   }
}

//Called when clicking 'Add to Basket'       
function addStockItemToBasket(stockItemId) {
	if(detectmob() == true) {
		//Phone so direct to page
		window.location.href = '/customerOrder/addStockItemToCustomerOrderForMobile?id=' + stockItemId;
		return;
	}
	
	//Desktop, use ajax
	$.getJSON('/customerOrder/' + stockItemId, function(data) {

		//console.log(data); Doesn't work on IE
		var imgHtml = '';
		if(data['imageFilename'] != null) {
			imgHtml = "<img src='/imageFiles/150/" + data['imageFilename'] + "' width='10%' class='pull-left' style='padding-right : 10px;'></img>"
		}
		var information = imgHtml + "You have added <b>" + data['title'] + "</b> to your basket! Press buttons below to view your basket or continue shopping.";
		$('div#information').html(information);
		$('div#showAddedStockItemToCustomerOrderModel').modal('show');
	}); 
}