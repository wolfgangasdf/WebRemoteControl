
var socket = null;
var hammer = null;
var hammertr = null;
var queue = [];
var FACTOR = 4.0;
var intervalid = null;

function debug(s) {
    console.log(s);
}

function menuchanged() {
    'use strict';
    var vis = document.getElementsByClassName('_vis_');
    var targetid = document.getElementById('menu').value;
    var target = document.getElementById(targetid);
    if (vis.length !== 0) {
        vis[0].className = vis[0].className.replace('_vis_','_inv_');
    }
    if (target !== null ) {
        target.className = target.className.replace('_inv_','_vis_');
        var tpa = document.getElementById("cont-trackpadarea");
        if (targetid == "cont-files") {
            tpa.className = tpa.className.replace('_vis_','_inv_');
        } else {
            tpa.className = tpa.className.replace('_inv_','_vis_');
            loadtrackpad();
        }
        if (targetid == "cont-files") {
            queue.push("fbgetfiles")
        }
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
        dx = Math.round(FACTOR * (ev.deltaX - prevDeltaX));
        dy = Math.round(FACTOR * (ev.deltaY - prevDeltaY));

        prevDeltaX = ev.deltaX;
        prevDeltaY = ev.deltaY;

        if(canEmit){
            queue.push('move,' + dx + ',' + dy);
            canEmit = false;
            setTimeout(function(){ canEmit = true; }, 50);
        }
    }

    hammer.on('pan', panDrag);

    hammer.on('scroll', function(ev) {
        scrollAmount = ev.deltaY + (prevScrollAmount*-1);
        prevScrollAmount = ev.deltaY;
        if(canEmit){
            queue.push('scroll,' + scrollAmount);
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

// init websocket. call after connection loss.
function initwebsocket() {
    socket = new WebSocket("ws://" + window.location.hostname + ":8001");

    socket.onopen = function(){
        loadtrackpad();
        loadswiper();
        debug("socket.onopen: initialized!");
    }

    socket.onclose = function(){
        debug("socket.onclose");
        clearInterval(intervalid);
        setTimeout(function(){ initwebsocket(); }, 250);
    }

    // react
    socket.onmessage = function (event) {
        ss = event.data.split(",");
        switch(ss[0]) {
            case "cmdlist":
                select = document.getElementById('cmd');
                for (var i = 1; i<ss.length; i++){
                    var opt = document.createElement('option');
                    opt.value = ss[i];
                    opt.innerHTML = ss[i];
                    select.appendChild(opt);
                }
                break;
            case "fblist":
                s = "";
                for (i = 1; i < ss.length; i++) {
                    s += "<tr><td><input type=\"button\" value=\"" + ss[i] + "\"></td></tr>";
                }
                document.getElementById('fbtable').innerHTML = s;
                break;
            default:
                alert("received unknown command " + ss[0]);
                break;
        }
    }

    intervalid = setInterval(function(){
        if (socket.readyState == socket.OPEN) {
             var toEmit = queue.shift();
             if (toEmit) { socket.send(toEmit); }
        }
    },25);
}


window.onload = function() {

    // https://github.com/B1naryStudio/js-mobile-console
    mobileConsole.show();
    mobileConsole.options({ showOnError: true, proxyConsole: false, isCollapsed: true, catchErrors: true });
    mobileConsole.toggleCollapsed();

    initwebsocket();

    // events
    document.getElementById('textinput').onkeydown = function(event) {
        if (event.keyCode == 13) {
            queue.push("text," + document.getElementById('textinput').value);
        }
    }

    function pushclosure(a, b) { return function () { queue.push(a + "," + b) }; }

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
        queue.push("cmd," + document.getElementById("cmd").value)
        document.getElementById("cmd").selectedIndex = 0;
    });

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
        queue.push("fbopen," + rX)
    });

    debug("window.onload: initialized!");
}
