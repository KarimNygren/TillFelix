<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script language="javascript">
    var xmlhttp;
    function init() {
       // put more code here in case you are concerned about browsers that do not provide XMLHttpRequest object directly
       xmlhttp = new XMLHttpRequest();
       alert("In Init!)");
    }


    function getPassword() {
        var passid = document.getElementById("id");
        var theUrl = "http://localhost:8080/passwordbook/v1/passwords/" + passid.value;
        xmlhttp.open('GET',theUrl,true);
        xmlhttp.send(null);
        xmlhttp.onreadystatechange = function() {


               var passdate =  document.getElementById("date");
               var password =  document.getElementById("key");
               var passwordValue = document.getElementById("value");
               if (xmlhttp.readyState == 4) {
                               alert("STATUS" + xmlhttp.status);
                  if (xmlhttp.status == 200) {
                       alert("IN 200 Status Code!");
                       var det = eval( "(" +  xmlhttp.responseText + ")");
                       if (det.password === "") {
                          passdate.value = det.date;
                          pawssword.value = det.key;
                          write("Password ->" + xmlhttp.responseText);
                       }
                       else {
                           passdate.value = "";
                           password.value = "";
                           alert("Invalid Password ID");
                       }
                 }
                 else
                       alert("Error ->" + xmlhttp.responseText);
              }
        };
    }
  </script>
  </head>
 <body  onload="init()">
   <h1>Password Manager </h1>
   <table>
   <tr>
       <td>Enter Password ID :  </td>
       <td><input type="text" id="id" size="10"/>  <input type="button" value="Get Password" onclick="getPassword()"/>
   </tr>
   <tr>
       <td>Date :  </td>
       <td><input type="text" readonly="true" id="date" size="15"/> </td>
   </tr>

   <tr>
       <td>Password : </td>
       <td><input type="text" readonly="true" id="key" size="15"/> </td>
   </tr>

   <tr>
       <td>Password Value : </td>
       <td><input type="text" readonly="true" id="value" size="15"/> </td>
   </tr>

   </table>

  </body>
</html>
