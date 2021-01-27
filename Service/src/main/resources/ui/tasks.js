var urlPrefix = "http://"+ $(location).attr('host'); // location.host gets the hostname and port(if specified) of the current URL

function performTask() {
    $.ajax(
    {
            beforeSend: function (xhr) {
                xhr.setRequestHeader("Authorization", "Basic " + btoa($('#userName').val() +":"+ $('#userPwd').val()));
            },
            type: "POST",
            url: urlPrefix+"/auth2/login",
            dataType: "json",

            success: function(result,status,xhr) {

                $.ajax({
                    type: "GET",
                    url: urlPrefix+"/download/exporter",
                    xhrFields: { withCredentials: true },
                    success: function (result, status, xhr) {


                        var name = xhr.getResponseHeader('Content-Disposition').split("=");
                        var a = document.createElement("a");
                        a.text = "Download "+name[1];
                        a.href = name[1];
                        a.download = name[1];
                        document.body.appendChild(a);
                        console.log(name[1]);
                    },
                    error: function (xhr, status, error) {
                        console.log("Error occured while downloading file: " + error);
                    }
                })
            },

            error: function(xhr,status,error) {
                 console.log("Error occured on login. " + error);
            }

        }
    );
};


