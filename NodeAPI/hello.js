var http = require('http');
var net = require('net');
var url = require('url');
var querystring = require('querystring');
var StringDecoder = require('string_decoder').StringDecoder;
var HOST = '127.0.0.1';
var PORT = '4444';
var interval;
var outputMessage = '';
var urlObj = '';
var socketMessage = new Array();
var cookies = {};

global.messages = {
    'V8': new Array()
};

var http_method_funs =
	{
		'GET':function(resid,data,request,response)
		{
			//, 'Transfer-Encoding': 'chunked'
			
			request.headers.cookie && request.headers.cookie.split(';').forEach(
			        function(cookie){
			        	var parts = cookie.split('=');
			        	cookies[parts[0]] = (parts[1]);
			        }		
			     );
			
			if(cookies['mycookie']==null)
			{
			    response.writeHead(200, {'Content-Type': 'text/html; charset=utf-8'
                    ,'Set-Cookie':'mycookie='+ JavaScriptNewGuid.NewGuid()});
			}
			else
			{
			    response.writeHead(200, {'Content-Type': 'text/html; charset=utf-8'
			    	,'Set-Cookie':'mycookie='+cookies['mycookie']}); 
			}
			var interval = setTimeout(myoutput,500);
			
			response.connection.on('end',function()
					   {
				          clearInterval(interval);
				          response.write("clearInterval");
				       }
					);
			
			function myoutput()
			{
				var msgs = socketMessage;
				var currentlength = msgs.length; //对于临界资源msgs的操作，先取得当前时间片上的数组大小，然后再逐步弹出.
				                                 //因为Array push方法永远是在数组的最后添加进去的，所以在多线程环境中，不会对临界资源出现错误操作问题
				if(currentlength >0)
				{
					var str = socketMessage.join("\r\n");
					for(var i=0;i<currentlength;i++)
					{
						socketMessage.shift(); //取出数组中的第一个元素
					}
					
					response.write(str,'utf-8');

				}
				else
				{
					response.write("No result");
					response.write((socketMessage.length).toString());
					
			    }
				
				response.end();
			}	
		},
	};

var JavaScriptNewGuid =
{
    NewGuid:function()
	{
	   return (this.S4()+this.S4()+"-"+this.S4()+"-"+this.S4()+"-"+this.S4()+"-"+this.S4()+this.S4()+this.S4());
	},
	S4:function()
	{
	   return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
	}	
};

http.createServer(function (req, res) {
	urlObj = req.url;
    urlObj = String(urlObj.split("=")[1]).replace(/&_/,"");
    
    var temp = url.parse(urlObj,true,false);
    var qs = querystring.parse(temp.query);
    
        
	var client = new net.Socket();
	client.bufferSize = 100;
    client.connect(PORT,HOST, function(){
    	client.write(unescape(temp.href));
    	client.write('\r\n');
    	client.write('.');
    	client.write('\r\n');   	
    });
    
    client.on('data',function(data)
    		{
    	if(data.length>2){
    	       socketMessage.push(data);
    	}
    		});
    
    client.on('close',function()
    		{
    	        
    		});
    
    var method = req.method;
    if(typeof http_method_funs[method] == 'function')
    	{
    	   http_method_funs['GET'].call(null,'V8','',req,res)	   
    	}
    
}).listen(process.env.PORT);