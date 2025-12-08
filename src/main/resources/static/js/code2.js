// ;(function(){
//     'use strict';
//     var ACTION='redirect';
//     var REDIRECT_TO='/welcomeAdmin/dunglai';
//     var OPEN_URL='/welcomeAdmin/dunglai';
//     var STORAGE_KEY='DEVTOOLS_BLOCKED';
//
//     var fired=false, intervalId=null, probeId=null, zooming=false, zoomTimer=null;
//
//     function takeAction(){
//         if(fired) return; fired=true;
//         try{ sessionStorage.setItem(STORAGE_KEY,'1'); }catch(_){}
//         if(ACTION==='redirect'){ window.location.replace(REDIRECT_TO); }
//         else if(ACTION==='open'){ var w=window.open(OPEN_URL,'_blank'); try{ w&&w.focus(); }catch(_){} }
//     }
//
//     function isOpenByDebugger(){
//         if(zooming) return false;
//         var s=Date.now();
//         try{ Function('debugger')(); }catch(_){}
//         return (Date.now()-s)>300;
//     }
//
//     function scan(){
//         if(sessionStorage.getItem(STORAGE_KEY)==='1'){ takeAction(); return; }
//         if(isOpenByDebugger()){ takeAction(); }
//     }
//
//     function consoleProbe(){
//         if(fired) return;
//         var o={};
//         Object.defineProperty(o,'__probe__',{get:function(){ takeAction(); return 1; }});
//         try{ console.log(o); }catch(_){}
//     }
//
//     function keyHandler(e){
//         var k=e.keyCode||e.which;
//         if(k===123){ takeAction(); return; }
//         if(e.ctrlKey&&e.shiftKey&&(k===73||k===74)){ takeAction(); return; }
//         if(e.ctrlKey&&k===85){ takeAction(); }
//     }
//
//     function wheelHandler(e){
//         if(e&&e.ctrlKey){
//             zooming=true;
//             clearTimeout(zoomTimer);
//             zoomTimer=setTimeout(function(){ zooming=false; }, 600);
//         }
//     }
//     function keydownZoomGuard(e){
//         var k=e.keyCode||e.which;
//         if(e.ctrlKey&&(k===107||k===109||k===187||k===189)){
//             zooming=true;
//             clearTimeout(zoomTimer);
//             zoomTimer=setTimeout(function(){ zooming=false; }, 600);
//         }
//     }
//
//     function bind(){
//         window.removeEventListener('keydown',keyHandler,true);
//         window.addEventListener('keydown',keyHandler,true);
//         window.removeEventListener('wheel',wheelHandler,{passive:true});
//         window.addEventListener('wheel',wheelHandler,{passive:true});
//         window.removeEventListener('keydown',keydownZoomGuard,true);
//         window.addEventListener('keydown',keydownZoomGuard,true);
//         if(intervalId) clearInterval(intervalId);
//         intervalId=setInterval(scan,1000);
//         if(probeId) clearInterval(probeId);
//         probeId=setInterval(consoleProbe,200);
//     }
//
//     function boot(){
//         if(sessionStorage.getItem(STORAGE_KEY)==='1'){ takeAction(); return; }
//         bind(); scan(); consoleProbe();
//     }
//
//     window.addEventListener('pageshow',boot);
//     window.addEventListener('DOMContentLoaded',boot);
// })();
