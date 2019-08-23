 jQuery(document).ready(function($){
  	firebase.auth.Auth.Persistence.LOCAL;

  	$("#btn-login").click(function(){



  		var email = $("#email").val();
  		var password = $("#password").val();

  		var result = firebase.auth().signInWithEmailAndPassword(email, password);

  		result.catch(function(error){
  			var errorCode = error.code;
  			var errorMessage = error.errorMessage;

  			console.log(errorCode);
  			console.log(errorMessage);
  		});

  	});
  });