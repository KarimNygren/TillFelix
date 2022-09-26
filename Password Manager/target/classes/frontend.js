    var xmlhttp;

    function init() {
       xmlhttp = new XMLHttpRequest();
    }


    function getPassword() {

        var passid = document.getElementById("id");
        var theUrl = "http://localhost:8080/passwordbook/v1/passwords/" + passid.value;
        xmlhttp.open('GET',theUrl,true);
        xmlhttp.send(null);
        xmlhttp.onreadystatechange = function() {
                var passdate =  document.getElementById("jsdate");
                var password =  document.getElementById("jskey");
                var passwordValue = document.getElementById("jsvalue");
            if (xmlhttp.readyState == 4) {
                if (xmlhttp.status == 200) {

                    var det = eval( "(" +  xmlhttp.responseText + ")");

                    if (det.value) {
                          passdate.value = det.date;
                          password.value = det.key;
                          passwordValue.value = det.value;
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

    function getAllPasswords() {
        var aUrl = "http://localhost:8080/passwordbook/v1/passwords/summary";
        xmlhttp.open('GET',aUrl,true);
        xmlhttp.send(null);
        xmlhttp.onreadystatechange = function() {

            if (xmlhttp.readyState == 4) {
                if (xmlhttp.status == 200) {
                    var json = eval( "(" +  xmlhttp.responseText + ")");

                    if(json.length > 0){
                    // Creates a list dynamically depending on how many elements can be found in the list
                        function makeUL(json) {
                            // Create the list element:
                            var list = document.createElement('ul');

                            for(var i = 0; i < Object.keys(json).length; i++) {
                                // Create the list item:
                                var item = document.createElement('li');

                                item.appendChild(document.createElement('pre')).innerHTML = json[i].key + " - " + json[i].value + "\n(" + json[i].id + ")";

                                // Add it to the list:
                                list.appendChild(item);
                            }

                            // Finally, return the constructed list:
                            return list;
                        }
                        // Add the contents of json to the div:
                        document.getElementById('pw-list').appendChild(makeUL(json));

                    }
                }
            }
        };
    }

    function createPassword() {
        var createKey = document.getElementById("ckey");
        var createValue = document.getElementById("cvalue");

        var obj = new Object();
        obj.key = createKey.value;
        obj.value = createValue.value;
        var jsonString= JSON.stringify(obj);

        var aUrl = "http://localhost:8080/passwordbook/v1/passwords/";

        xmlhttp.open('POST',aUrl,true);
        xmlhttp.send(jsonString);
        xmlhttp.onreadystatechange = function() {

            var theOutput = document.getElementById("responseOutput");

            if (xmlhttp.readyState == 4) {
                if (xmlhttp.status == 201) {
                    var adet = xmlhttp.responseText;
                    theOutput.value = adet;
                }
            }
        };
    }

        function deletePassword() {
            var deleteKey = document.getElementById("dkey");

            var aUrl = "http://localhost:8080/passwordbook/v1/passwords/delete/" + deleteKey.value;

            var xmlhttp = new XMLHttpRequest();

            xmlhttp.open('POST',aUrl,true);
            xmlhttp.onreadystatechange = function() {

                var theOutput = document.getElementById("deleteResponseOutput");

                if (xmlhttp.readyState === 4) {

                    if (xmlhttp.status == 204 || xmlhttp.status == 200) {

                        theOutput.value = "The password has been deleted."
                    }
                }
            };
            xmlhttp.send();
        }

