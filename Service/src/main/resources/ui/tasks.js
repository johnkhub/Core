var urlPrefix = "http://"+ $(location).attr('host'); // location.host gets the hostname and port(if specified) of the current URL

function performTask() {
    document.getElementById("a").innerText = "";
    $.ajax(
    {
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Basic " + btoa($('#userName').val() +":"+ $('#userPwd').val()));
            },
            type: "POST",
            url: urlPrefix+"/auth2/login",
            dataType: "json",

            success: function(result,status,xhr) {
                let expSelector = document.getElementById("expSelector");
                let urlSuffix = expSelector.options[expSelector.selectedIndex].value;
                $.ajax({
                    type: "GET",
                    url: urlPrefix+"/download/"+urlSuffix,
                    xhrFields: { withCredentials: true },
                    success: function (result, status, xhr) {
                        var name = xhr.getResponseHeader('Content-Disposition').split("=");
                        var a = document.getElementById("a");
                        a.text = "Download "+name[1];
                        a.href = name[1];
                        a.download = name[1];
                        console.log(name[1]);
                    },
                    error: function (xhr, status, error) {
                        var a = document.getElementById("a");
                        a.text = "Exporter error:  "+error;
                        console.log("Error occured while downloading file: " + error);
                    }
                })
            },

            error: function(xhr,status,error) {
                var a = document.getElementById("a");
                a.innerText = "Login error: "+error;
                console.log("Error occured on login. " + error);
            }

        }
    );
};


