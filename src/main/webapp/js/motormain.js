var ebcdic = {
    A:1, B:2, C:3,
    D:4, E:5, F:6,
    G:7, H:8, I:9,
    J:1, K:2, L:3,
    M:4, N:5, O:0,
    P:7, Q:8, R:9,
    S:2, T:3, U:4,
    V:5, W:6, X:7,
    Y:8, Z:9,
    "%C4":1,
    "%D6":0,
    "%DC":4,
    0:0, 1:1, 2:2, 3:3, 4:4, 5:5, 6:6, 7:7, 8:8, 9:9
};

var weight=[9, 8, 7, 6, 5, 4, 3, 2, 10, 9, 8, 7, 6, 5, 4, 3, 2];

var messages = {
    vinEnter : "${message:txt.input.vin.placeholder}",
    mileageEnter : "${message:txt.input.mileage.placeholder}",
    vinSearch : "${message:txt.input.vin.placeholder}"
};

function check(element, containerId) {
    if(element.value.length == 17) {
        var inputText = element.value.toUpperCase();
        var arrayText = inputText.split("");
        var result = 0, size = inputText.length;

        for(var i = 0; i < size; i++) {
            var fromEbcDic = ebcdic[escape(arrayText[i])];

            if(fromEbcDic == null) {
                return;
            }

            result += fromEbcDic * weight[i]
        }

        result = result % 11;

        if(result == 10) {
            result = "X";
        }

        document.getElementById('checkDigit' + containerId).innerHTML = result;
        document.getElementById('container' + containerId).style.display = "block";
    }
}

function handleFocus(element) {
    element.className += " active";
    tooglePlaceholder(element);
}
function handleBlur(element) {
    element.className = "round";
    tooglePlaceholder(element);
}
function tooglePlaceholder(element) {
   if(element.value === messages[element.id]) {
       element.style.color = "#777";
       element.value = "";
   } else if(element.value === "") {
       element.style.color = "#999";
       element.value = messages[element.id];
   }
}
