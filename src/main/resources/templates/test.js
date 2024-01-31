const response = await fetch("localhost:8080/experiment");
const experiment = await response.json();
console.log(experiment);

$(document).ready(function(){
    $("p").click(function(){
        console.log("Hola")
    });
  });

  