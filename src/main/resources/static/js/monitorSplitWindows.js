$(function(){
//alert("ok");
//鼠标横向、竖向操作对象
    var thisHorizontalObject,thisVerticalObject;
//文档对象
    var doc = document;
//查找横向分割栏
    var horizontalLabels = $(".hj-wrap").find('.hj-transverse-split-label');
//查找竖向分割栏
    var verticalLabels = $(".hj-wrap").find('.hj-vertical-split-label');

//定义一个对象
    function PointerObject(){
        this.el = null;//当前鼠标选择的对象
        this.clickX =0;//鼠标横向初始位置
        this.clickY =0;//鼠标竖向初始位置
        this.horizontalDragging=false;//判断鼠标可否横向拖动
        this.verticalDragging=false;//判断鼠标可否竖向拖动
    }

    //鼠标按下事件
    /*doc.onmousedown = function(e){
        horizontalLabels = $(".hj-wrap").find('.hj-transverse-split-label');
        verticalLabels = $(".hj-wrap").find('.hj-vertical-split-label');
//判断窗体个数，并初始化窗体宽度
        /!*if($(".hj-wrap").length>0){
            for(var i=0;i<$(".hj-wrap").length;i++){
                var hjDivNums = $($(".hj-wrap")[i]).children(".hj-transverse-split-div");
                var defaultWidth =Math.floor(100/hjDivNums.length);
                $($(".hj-wrap")[i]).children(".hj-transverse-split-div").width(defaultWidth-1+"%");
            }
        }*!/
//横向分隔栏绑定事件
        horizontalLabels.bind('mousedown',function(e){
            thisHorizontalObject = new PointerObject();
            thisHorizontalObject.horizontalDragging = true;//鼠标可横向拖动
            thisHorizontalObject.el = this;
            thisHorizontalObject.clickX = e.pageX;//记录鼠标横向初始位置
        });

//竖向分隔栏绑定事件
        verticalLabels.bind('mousedown',function(e){
            thisVerticalObject = new PointerObject();
            thisVerticalObject.verticalDragging = true;//鼠标可竖向拖动
            thisVerticalObject.el = this;
            thisVerticalObject.clickY = e.pageY;//记录鼠标竖向初始位置
        });
    }*/

//横向分隔栏绑定事件
    horizontalLabels.bind('mousedown',function(e){
        thisHorizontalObject = new PointerObject();
        thisHorizontalObject.horizontalDragging = true;//鼠标可横向拖动
        thisHorizontalObject.el = this;
        thisHorizontalObject.clickX = e.pageX;//记录鼠标横向初始位置
    });

//竖向分隔栏绑定事件
    verticalLabels.bind('mousedown',function(e){
        console.log("----");
        thisVerticalObject = new PointerObject();
        thisVerticalObject.verticalDragging = true;//鼠标可竖向拖动
        thisVerticalObject.el = this;
        thisVerticalObject.clickY = e.pageY;//记录鼠标竖向初始位置
    });

    //鼠标移动事件
    doc.onmousemove = function(e){
//鼠标横向拖动
        if(thisHorizontalObject != null){
            if (thisHorizontalObject.horizontalDragging) {
                var changeDistance = 0;
                var nextWidth = $(thisHorizontalObject.el).next().width();
                var prevWidth = $(thisHorizontalObject.el).prev().width();
                if(thisHorizontalObject.clickX>=e.pageX){
//鼠标向左移动
                    changeDistance = Number(thisHorizontalObject.clickX)-Number(e.pageX);
                    if($(thisHorizontalObject.el).prev().width()-changeDistance<20){

                    }else{
                        $(thisHorizontalObject.el).prev().width($(thisHorizontalObject.el).prev().width()-changeDistance);
                        $(thisHorizontalObject.el).next().width($(thisHorizontalObject.el).next().width()+changeDistance);
                        thisHorizontalObject.clickX=e.pageX;
                        $(thisHorizontalObject.el).offset({left:e.pageX-4});
                    }
                }else{
//鼠标向右移动
                    changeDistance = Number(e.pageX)-Number(thisHorizontalObject.clickX);
                    if($(thisHorizontalObject.el).next().width()-changeDistance<20){

                    }else{
                        $(thisHorizontalObject.el).prev().width($(thisHorizontalObject.el).prev().width()+changeDistance);
                        $(thisHorizontalObject.el).next().width($(thisHorizontalObject.el).next().width()-changeDistance);
                        thisHorizontalObject.clickX=e.pageX;
                        $(thisHorizontalObject.el).offset({left:e.pageX-4});
                    }
                }
                $(thisHorizontalObject.el).width(10);
            }
        }
//鼠标竖向拖动
        if(thisVerticalObject != null){
            if (thisVerticalObject.verticalDragging) {
                var changeDistance = 0;
                var nextheight = $(thisVerticalObject.el).next().height();
                var prevheight = $(thisVerticalObject.el).prev().height();
                if(thisVerticalObject.clickY>=e.pageY){
//鼠标向上移动
                    changeDistance = Number(thisVerticalObject.clickY)-Number(e.pageY);
                    if($(thisVerticalObject.el).prev().height()-changeDistance<20){

                    }else{
                        $(thisVerticalObject.el).prev().height($(thisVerticalObject.el).prev().height()-changeDistance);
                        $(thisVerticalObject.el).next().height($(thisVerticalObject.el).next().height()+changeDistance);
                        thisVerticalObject.clickY=e.pageY;
                        $(thisVerticalObject.el).offset({top:e.pageY-4});
                    }
                }else{
//鼠标向下移动
                    changeDistance = Number(e.pageY)-Number(thisVerticalObject.clickY);
                    if($(thisVerticalObject.el).next().height()-changeDistance<20){

                    }else{
                        $(thisVerticalObject.el).prev().height($(thisVerticalObject.el).prev().height()+changeDistance);
                        $(thisVerticalObject.el).next().height($(thisVerticalObject.el).next().height()-changeDistance);
                        thisVerticalObject.clickY=e.pageY;
                        $(thisVerticalObject.el).offset({top:e.pageY-4});
                    }
                }
                $(thisVerticalObject.el).height(5);
            }
        }
    };

    //鼠标松开事件
    $(doc).mouseup(function(e) {
//鼠标释放时判断是否有横向操作对象
        if (thisHorizontalObject != null) {
            thisHorizontalObject.horizontalDragging = false;//修改横向可拖动状态
            thisHorizontalObject = null;//当鼠标释放的时候，销毁横向对象
        }
//鼠标释放时判断是否有竖向操作对象
        if (thisVerticalObject != null) {
            thisVerticalObject.verticalDragging = false;//修改竖向可拖动状态
            thisVerticalObject = null;//当鼠标释放的时候，销毁竖向对象
        }

        e.cancelBubble = true;
    });


});