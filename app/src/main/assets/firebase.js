jQuery(document).ready(function($){
var db1 = firebase.database().ref('PhoneBook/');
var data;
db1.once("value",snap=>{
		data=snap.val();
		
		
		 $.each(data, function(key, val1){
  	


  		$.each(val1, function(key, val2){
  	
var table = $('#phonebook').DataTable();

  	     	table.row.add($(
    '<tr>' +
    '<td>'+val2.name+'</td>' +
    '<td>'+val2.mobile+'</td>' +
    '<td>'+val2.email+'</td>' +
    '<td>'+val2.address+'</td>' +
    '</tr>'
  	     
  	     	
)).draw(false);


  	     

  		


  });  


  });  





	});

var db2 = firebase.database().ref('Users/');
var data2;
db2.once("value",snap=>{
		data2=snap.val();

		


		$.each(data2, function(key, val1){
			
			var table = $('#users').DataTable();

  	     	table.row.add($(
    '<tr>' +
    '<td>'+val1.name+'</td>' +
    '<td>'+val1.uid+'</td>' +
    '<td>'+val1.status+'</td>' +
    '<td>'+val1.device_token+'</td>' +
    '</tr>'
  	     
  	     	
)).draw(false);
		});
});

var db3 = firebase.database().ref('Users/');
var data3;
db2.once("value",snap=>{
		data3=snap.val();

		


		$.each(data3, function(key, val1){
			
			var table = $('#Access').DataTable();

  	     	table.row.add($(
    '<tr>' +
    '<td>'+val1.name+'</td>' +
    '<td>'+val1.uid+'</td>' +
     '<td>'+val1.Login.Date+'</td>' +
    '<td>'+val1.Login.Time+'</td>' +
    '<td>'+val1.LogOut.Date+'</td>' +
    '<td>'+val1.LogOut.Time+'</td>' +
    '</tr>'
  	     
  	     	
)).draw(false);

var table1 = $('#location').DataTable();

  	     	table1.row.add($(
    '<tr>' +
    '<td>'+val1.name+'</td>' +
    '<td>'+val1.uid+'</td>' +
    '<td>'+val1.Login.Latitude+' / ' + val1.Login.Longitude +'</td>' +
    '<td>'+val1.LogOut.Latitude+' / ' + val1.LogOut.Longitude +'</td>' +
   
    '</tr>'
  	     
  	     	
)).draw(false);



		});
});

var db4 = firebase.database().ref('Groups/');

var data4;

db4.once("value", snap=>{

	data4=snap.val();
	

	$.each(data4, function(key, val1){

		


	$.each(val1, function(key, val2){

		if(val2.type == "image"){
			
			var cards = $();

                    
            cards = cards.add(create(val2.message));

			$('#gal').append(cards);

		}


		


	});


	});

  var user = firebase.auth().currentUser.uid;

  var db5 = firebase.database().ref('Users/');
                var data5;
                db5.once("value",snap=>{
                data5=snap.val();
                var emp = 0, cust = 0;

                


                $.each(data5, function(key, val1){
                
                
                
                    
                    if((val1.Type == "1") && (val1.bId == user)){
                      
                        emp = emp + 1;
                    
                    }else if(val1.Type == "2"){
                        cust = cust + 1;
                    }
            
                
                });
                
                $('#cust').html(cust);
                $('#emp').html(emp);

                });
var businessId = firebase.auth().currentUser.uid;

var dbo = firebase.database().ref('Users/');
            var datao;
            dbo.once("value",snap=>{
            datao=snap.val();

            console.log(datao);


            $.each(datao, function(key, val1){
            console.log(val1);
            

            if(val1.bId == businessId){
            var table = $('#userso').DataTable();
            table.row.add($(
             '<tr>' +
                '<td>'+val1.name+'</td>' +
                '<td>'+val1.uid+'</td>' +
                '<td>'+val1.status+'</td>' +
                '<td>'+val1.device_token+'</td>' +
                '</tr>'
                     
                      
            )).draw(false);
                }
});
          });

            var dbo2 = firebase.database().ref('Users/');
            var datao2; 
            dbo2.once("value",snap=>{
            datao2=snap.val();

            console.log(datao2);


            $.each(datao2, function(key, val1){
            console.log(val1);


            if(val1.bId == businessId){


            var table = $('#Accesso').DataTable();

            table.row.add($(
                '<tr>' +
                '<td>'+val1.name+'</td>' +
                '<td>'+val1.uid+'</td>' +
                 '<td>'+val1.Login.Date+'</td>' +
                '<td>'+val1.Login.Time+'</td>' +
                '<td>'+val1.LogOut.Date+'</td>' +
                '<td>'+val1.LogOut.Time+'</td>' +
                '</tr>'
                     
            
            )).draw(false);
        }

        });
        });


            var dbo3 = firebase.database().ref('Users/');
            var datao3;
            dbo3.once("value",snap=>{
            datao3=snap.val();

            console.log(datao3);


            $.each(datao3, function(key, val1){
            console.log(val1);
            

            if(val1.bId == businessId){
            var table1 = $('#locationo').DataTable();

            table1.row.add($(
    '<tr>' +
    '<td>'+val1.name+'</td>' +
    '<td>'+val1.uid+'</td>' +
    '<td>'+val1.Login.Latitude+' / ' + val1.Login.Longitude +'</td>' +
    '<td>'+val1.LogOut.Latitude+' / ' + val1.LogOut.Longitude +'</td>' +
   
    '</tr>'
         
            
)).draw(false);
                }



                });
            });




});

function create(data) {
    	var uid = 2;
        var cardTemplate = [
        '<div class="col-sm-6 col-md-4"> <a class="lightbox" href="',data,'"> <img src="',data,'" > </a> </div>'
        ];
        return $(cardTemplate.join(''));
    }
 




});

 

