
var trackfactor = 4.0; // trackpad scale factor
var socketrestartms = 250; // min time between socket reconnect
var clearqueuems = 2000; // clear socket queue if disconnected after this
var intervalms = 25; // interval ms for sending data over socket (25)

// internal
var lastInfo = "";
var socket = null;
var hammer = null;
var hammertr = null;
var queue = [];
var socketrestarting = false;
var socketlastrestartms = 0;
var socketlastfailms = 0;
var fbloadedfiles = false; // to keep highlight until folder changed

function debug(s) {
    console.log("[" + window.location.hash.substr(1) + "] " + s);
}

'use strict';
function menuchanged(reloadcontent = true) {
    var targetid = document.getElementById('menu').value;
    document.getElementById("cont-basic").classList.add('_inv_');
    document.getElementById("cont-vlc").classList.add('_inv_');
    document.getElementById("cont-files").classList.add('_inv_');
    document.getElementById("cont-files2").classList.add('_inv_');
    document.getElementById("cont-history").classList.add('_inv_');
    document.getElementById("cont-history2").classList.add('_inv_');
    document.getElementById("cont-trackpadarea").classList.add('_inv_');

    if (targetid == "basic") {
        document.getElementById("cont-basic").classList.remove('_inv_');
        document.getElementById("cont-trackpadarea").classList.remove('_inv_');
        loadtrackpad();
    } else if (targetid == "vlc") {
        document.getElementById("cont-vlc").classList.remove('_inv_');
        document.getElementById("cont-trackpadarea").classList.remove('_inv_');
        loadtrackpad();
    } else if (targetid == "files") {
        document.getElementById("cont-files").classList.remove('_inv_');
        document.getElementById("cont-files2").classList.remove('_inv_');
        if (reloadcontent && !fbloadedfiles) queue.push("fbgetfiles");
    } else if (targetid == "history") {
        document.getElementById("cont-history").classList.remove('_inv_');
        document.getElementById("cont-history2").classList.remove('_inv_');
        if (reloadcontent) queue.push("hgethistory");
    }
}

function incdecmenu(increment) {
    var menu = document.getElementById('menu')
    var newidx = menu.selectedIndex + (increment ? 1 : -1);
    if (newidx >= menu.length) newidx = 0;
    if (newidx < 0) newidx = menu.length - 1;
    menu.selectedIndex = newidx;
    menuchanged();
}

function loadswiper() {
    var swipediv = document.getElementById('swipecontent');
    if (hammertr !== null) { hammertr.destroy(); }
    hammertr = new Hammer(swipediv, { recognizers: [ [Hammer.Swipe,{ direction: Hammer.DIRECTION_HORIZONTAL }]] });
    hammertr.on('swipeleft', function (ev) {incdecmenu(true)});
    hammertr.on('swiperight', function (ev) {incdecmenu(false)});
}

function loadtrackpad() {
    var trackpad = document.getElementById('cont-trackpadarea');
    var scroll;
    var secondaryTap;
    var prevDeltaX = 0;
    var prevDeltaY = 0;
    var scrollAmount = 0;
    var prevScrollAmount = 0;
    var canEmit = true;

    if (hammer !== null) { hammer.destroy(); }
    hammer = Hammer(trackpad);

    scroll = new Hammer.Pan({
        event: 'scroll',
        direction: Hammer.DIRECTION_VERTICAL,
        pointers: 2
    });

    drag = new Hammer.Pan({
        event: 'drag',
        direction: Hammer.DIRECTION_VERTICAL,
        pointers: 3
    });

    secondaryTap = new Hammer.Tap({
        event: 'secondaryTap',
        pointers: 2
    });

    hammer.add(scroll);
    hammer.add(drag);
    hammer.add(secondaryTap);

    function panDrag(ev) {
        dx = Math.round(trackfactor * (ev.deltaX - prevDeltaX));
        dy = Math.round(trackfactor * (ev.deltaY - prevDeltaY));

        prevDeltaX = ev.deltaX;
        prevDeltaY = ev.deltaY;

        if(canEmit){
            queue.push('move\t' + dx + '\t' + dy);
            canEmit = false;
            setTimeout(function(){ canEmit = true; }, 50);
        }
    }

    hammer.on('pan', panDrag);

    hammer.on('scroll', function(ev) {
        scrollAmount = ev.deltaY + (prevScrollAmount*-1);
        prevScrollAmount = ev.deltaY;
        if(canEmit){
            queue.push('scroll\t' + scrollAmount);
            canEmit = false;
            setTimeout(function() { canEmit = true; }, 50);
        }
    });

    hammer.on('dragstart', function(ev) {
        queue.push('dragStart');
    });

    hammer.on('drag', panDrag);

    hammer.on('dragend', function(ev) {
        queue.push('dragEnd');
    });

    hammer.on('scrollend', function(ev) {
        prevScrollAmount = 0;
    });

    hammer.on('panend', function(ev) {
        prevDeltaX = 0;
        prevDeltaY = 0;
    });

    hammer.on('tap', function(ev) {
        queue.push('tap');
    });

    hammer.on('secondaryTap', function(ev) {
        queue.push('tap2');
    });
}

function showPage(idx, reloadcontent=false) {
    if (menu.selectedIndex != idx) {
        menu.selectedIndex = idx;
        menuchanged(reloadcontent);
    }
}

// init websocket. call after connection loss.
function initwebsocket() {
    debug("initwebsocket");
    socket = new WebSocket("ws://" + window.location.hostname + ":" + window.location.port + "/docs/" + window.location.hash.substr(1));
    socketlastrestartms = +new Date();
    debug("initwebsocket: socket created " + socket.readyState);
    socket.onopen = function(){
        debug("socket.onopen: initialized!");
        socketrestarting = false;
    }

    socket.onclose = function(){
        debug("socket.onclose");
        socketrestarting = false;
    }

    // react
    socket.onmessage = function (event) {
        ss = event.data.split("\t");
        debug("onmessage: " + ss[0]);
        switch(ss[0]) {
            case "cmdlist":
                select = document.getElementById('cmd');
                while (select.lastChild.value !== "url") { select.removeChild(select.lastChild); } // remove all but first
                for (var i = 1; i<ss.length; i++){
                    var opt = document.createElement('option');
                    opt.value = ss[i];
                    opt.innerHTML = ss[i];
                    select.appendChild(opt);
                }
                break;
            case "fblist":
                document.getElementById('currentpath').innerHTML = ss[1];
                s = "";
                for (i = 2; i < ss.length; i++) {
                    s += "<tr><td><input type=\"button\" value=\"" + ss[i] + "\"></td></tr>";
                }
                document.getElementById('fbtable').innerHTML = s;
                fbloadedfiles = true;
                var rows = document.querySelectorAll('#fbtable tr');
                if (rows.length > 0) rows[0].scrollIntoView({ block: 'center' });
                break;
            case "fbreveal": // rownumber, highlightode [0 nothing, 1 highlight additive, 2 remove all highlights before]
                showPage(2, false);
                let rowi=Number(ss[1]);
                let mode=Number(ss[2]);
                var rows = document.querySelectorAll('#fbtable tr');
                rows[rowi].scrollIntoView({ block: 'center' });
                if (mode == 2) {
                    for (let i=0; i<rows.length; i++) rows[i].classList.remove('highlight')
                }
                if (mode > 0) {
                    rows[rowi].className = "highlight";
                }
                break;
            case "showvlc":
                showPage(1, false);
                break;
            case "showimg":
                document.getElementById('imgrow').classList.remove('_inv_')
                break;
            case "hideimg":
                document.getElementById('imgrow').classList.add('_inv_')
                break;
            case "hlist":
                s = "";
                for (i = 1; i < ss.length; i++) {
                    s += '<tr><td><button id="hdelete" class="hdelete" onclick="hdelete(' + i + ')">&#x274c;</button>' + "<input type=\"button\" value=\"" + ss[i] + "\"></td></tr>";
                }
                document.getElementById('htable').innerHTML = s;
                break;
            default:
                alert("received unknown command " + ss[0]);
                break;
        }
    }

}

function hdelete(i) {
    queue.push("hdelete\t" + (i - 1))
    queue.push("hgethistory");
}

// handles queue<>socket communication, reconnects
setInterval(function(){
    var info = "";
    if (socket == null)
        info = "0";
    else
        if (socket.readyState != socket.OPEN || socketrestarting) info = "E";
    if (info != lastInfo) {
        document.getElementById('info').innerHTML = info;
        lastInfo = info;
    }
    //debug("interval: len=" + queue.length + " socket=" + socket + " srstate=" + socket.readyState);
    if (queue.length != 0 && socket != null) {
        var msnow = +new Date();
        if (socket.readyState == socket.OPEN) {
            socketlastfailms = -1;
            var toEmit = queue.shift();
            if (toEmit) socket.send(toEmit);
        } else {
            if (socketlastfailms == -1) socketlastfailms = msnow;
            if (!socketrestarting && msnow-socketlastrestartms > socketrestartms) {
                debug("restart socket!");
                socketrestarting = true;
                initwebsocket();
            } else {
                if (msnow-socketlastfailms > clearqueuems) {
                    queue = []; // now this whole block is not called until user does stuff again
                    socketlastfailms = -1;
                }
            }
        }
    }
},intervalms);

window.onhashchange = function() {
    debug("onhashchange!")
    initwebsocket;
}

if (!window.location.hash) {
    const newDocumentId = Date.now().toString(36); // this should be more random
    window.history.pushState(null, null, "#" + newDocumentId);
}


window.onload = function() {
    mobileConsole.show(); // https://github.com/B1naryStudio/js-mobile-console
    mobileConsole.options({ showOnError: true, proxyConsole: false, isCollapsed: true, catchErrors: true });
    mobileConsole.toggleCollapsed();

    initwebsocket();

    loadtrackpad();
    loadswiper();

    // events
    document.getElementById('textinput').onkeydown = function(event) {
        if (event.keyCode == 13) {
            queue.push("text\t" + document.getElementById('textinput').value);
        }
    }

    function pushclosure(a, b) { return function () { queue.push(a + "\t" + b) }; }

    autos = document.getElementsByClassName('bauto')
    for (i = 0; i < autos.length; i++) {
        var myid = autos[i].id;
        autos[i].addEventListener('click', pushclosure("bauto", myid), false);
    }

    document.getElementById('menu').addEventListener('change', function() {
        menuchanged();
    });

    document.getElementById('cmd').addEventListener('change', function () {
        'use strict';
        queue.push("cmd\t" + document.getElementById("cmd").value)
        document.getElementById("cmd").selectedIndex = 0;
    });

    // close tab button / close image viewer, better handle separately
    document.getElementById('bclosetab').addEventListener('click', function() {
        queue.push("bclosetab")
    }, false);


    // file browser
    document.getElementById('fbup').addEventListener('click', function() {
        queue.push("fbup")
    }, false);

    document.getElementById('fbtable').addEventListener('click', function (e) {
        var target = e.target,
            row, col, rX, cX;
        if (target.type !== 'button') {return;}
        col = target.parentElement;
        row = col.parentElement;
        rX = row.rowIndex;
        queue.push("fbopen\t" + rX)
    });

    // image viewer
    document.getElementById('imgprev').addEventListener('click', function() {
        queue.push("imgprev")
    });
    document.getElementById('imgnext').addEventListener('click', function() {
        queue.push("imgnext")
    });

    // history
    document.getElementById('htable').addEventListener('click', function (e) {
        var target = e.target,
            row, col, rX, cX;
        if (target.type !== 'button') {return;}
        col = target.parentElement;
        row = col.parentElement;
        rX = row.rowIndex;
        queue.push("hopen\t" + rX)
    });

    debug("window.onload: initialized!");
}
