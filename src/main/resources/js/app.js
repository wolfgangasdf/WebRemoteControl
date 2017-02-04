window.onload = function(){
    var socket = new WebSocket("ws://" + window.location.hostname + ":8001");
    var hammer = null;
    var queue = [];

    loadtrackpad = function(){
      var trackpad = document.getElementById('trackpad-area');
      var width = trackpad.offsetWidth;
      var height = trackpad.offsetHeight;
      var x = parseInt(width/2);
      var y = parseInt(height/2);
      var scroll;
      var secondaryTap;
      var prevDeltaX = 0;
      var prevDeltaY = 0;
      var scrollDelta = 0;
      var scrollAmount = 0;
      var prevScrollAmount = 0;
      var clearDelta;
      var canEmit = true;

      setInterval(function(){
        var toEmit = queue.shift()
        if (toEmit)
          socket.send(toEmit)

      },25);

        socket.send('screen,' + width+","+height);

        if (hammer !== null) {
            socket.send('debug,destroying existing hammer...');
            hammer.destroy();
        }

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

      hammer.on('pan', function(ev) {
        x+= ev.deltaX - prevDeltaX;
        y+= ev.deltaY - prevDeltaY;

        prevDeltaX = ev.deltaX;
        prevDeltaY = ev.deltaY;

        if(canEmit){
          queue.push('move,' + x + ',' + y);
          canEmit = false;
          setTimeout(function(){
            canEmit = true;
          },50);
        }
      });

      hammer.on('scroll', function(ev){
        scrollAmount = ev.deltaY + (prevScrollAmount*-1);
        prevScrollAmount = ev.deltaY;
        if(canEmit){
          queue.push('scroll,' + scrollAmount);
          canEmit = false;
          setTimeout(function(){
            canEmit = true;
          },50);
        }
      });

      hammer.on('dragstart', function(ev) {
        queue.push('dragStart');
      });

      hammer.on('drag', function(ev) {
        x+= ev.deltaX - prevDeltaX;
        y+= ev.deltaY - prevDeltaY;

        prevDeltaX = ev.deltaX;
        prevDeltaY = ev.deltaY;

        if(canEmit){
          queue.push('move,' + x + ',' + y);
          canEmit = false;
          setTimeout(function(){
            canEmit = true;
          },50);
        }
      });

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
    };




    document.getElementById('textinput').onkeydown = function(event) {
      if (event.keyCode == 13) {
          queue.push("text," + document.getElementById('textinput').value)
      }
    }

    document.getElementById('bleft').addEventListener('click', function() {
        queue.push("key,37")
    }, false);
    document.getElementById('bright').addEventListener('click', function() {
        queue.push("key,39")
    }, false);
    document.getElementById('bup').addEventListener('click', function() {
        queue.push("key,38")
    }, false);
    document.getElementById('bdown').addEventListener('click', function() {
        queue.push("key,40")
    }, false);
    document.getElementById('bspace').addEventListener('click', function() {
        queue.push("key,32")
    }, false);
    document.getElementById('bescape').addEventListener('click', function() {
        queue.push("key,27")
    }, false);
    document.getElementById('bf').addEventListener('click', function() {
        queue.push("key,70")
    }, false);

    document.getElementById('menu').addEventListener('change', function () {
        'use strict';
        var vis = document.getElementsByClassName('_vis_');
        var target = document.getElementById(this.value);
        if (vis.length !== 0) {
            vis[0].className = vis[0].className.replace('_vis_','_inv_');
        }
        if (target !== null ) {
            target.className = target.className.replace('_inv_','_vis_');
            if (target.className.includes("trackpad")) { // could do always?
                loadtrackpad();
            }
        }
    });

    // this assumes that the trackpad div is shown initially!
    socket.onopen = function(){
        loadtrackpad();
    }

}
