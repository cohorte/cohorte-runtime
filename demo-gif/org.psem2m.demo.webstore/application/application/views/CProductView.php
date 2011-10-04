<?php ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=windows-1252" />
<title>Electronix Store</title>
<link rel="stylesheet" type="text/css" href="/<?php echo base_url(); ?>style.css" />
<!--[if IE 6]>
<link rel="stylesheet" type="text/css" href="iecss.css" />
<![endif]-->
<script>
PositionX = 100;
PositionY = 100;


defaultWidth  = 500;
defaultHeight = 500;
var AutoClose = true;

if (parseInt(navigator.appVersion.charAt(0))>=4){
var isNN=(navigator.appName=="Netscape")?1:0;
var isIE=(navigator.appName.indexOf("Microsoft")!=-1)?1:0;}
var optNN='scrollbars=no,width='+defaultWidth+',height='+defaultHeight+',left='+PositionX+',top='+PositionY;
var optIE='scrollbars=no,width=150,height=100,left='+PositionX+',top='+PositionY;
function popImage(imageURL,imageTitle){
if (isNN){imgWin=window.open('about:blank','',optNN);}
if (isIE){imgWin=window.open('about:blank','',optIE);}
with (imgWin.document){
writeln('<html><head><title>Loading...</title><style>body{margin:0px;}</style>');writeln('<sc'+'ript>');
writeln('var isNN,isIE;');writeln('if (parseInt(navigator.appVersion.charAt(0))>=4){');
writeln('isNN=(navigator.appName=="Netscape")?1:0;');writeln('isIE=(navigator.appName.indexOf("Microsoft")!=-1)?1:0;}');
writeln('function reSizeToImage(){');writeln('if (isIE){');writeln('window.resizeTo(300,300);');
writeln('width=300-(document.body.clientWidth-document.images[0].width);');
writeln('height=300-(document.body.clientHeight-document.images[0].height);');
writeln('window.resizeTo(width,height);}');writeln('if (isNN){');       
writeln('window.innerWidth=document.images["George"].width;');writeln('window.innerHeight=document.images["George"].height;}}');
writeln('function doTitle(){document.title="'+imageTitle+'";}');writeln('</sc'+'ript>');
if (!AutoClose) writeln('</head><body bgcolor=ffffff scroll="no" onload="reSizeToImage();doTitle();self.focus()">')
else writeln('</head><body bgcolor=ffffff scroll="no" onload="reSizeToImage();doTitle();self.focus()" onblur="self.close()">');
writeln('<img name="George" src='+imageURL+' style="display:block"></body></html>');
close();		
}}

</script>
<script type="text/javascript" src="/<?php echo base_url(); ?>js/boxOver.js"></script>
</head>
<body>

<div id="main_container">
	<div class="top_bar">
    	<div class="top_search">
        	<div class="search_text"><a href="#">Advanced Search</a></div>
            <input type="text" class="search_input" name="search" />
            <input type="image" src="/<?php echo base_url(); ?>images/search.gif" class="search_bt"/>
        </div>
        
        <div class="languages">
        	<div class="lang_text">Languages:</div>
            <a href="#" class="lang"><img src="/<?php echo base_url(); ?>images/en.gif" alt="" title="" border="0" /></a>
            <a href="#" class="lang"><img src="/<?php echo base_url(); ?>images/de.gif" alt="" title="" border="0" /></a>       
        </div>
    
    </div>
	<div id="header">
        
        <div id="logo">
            <a href="index.html"><img src="/<?php echo base_url(); ?>images/logo.png" alt="" title="" border="0" width="237" height="140" /></a>
	    </div>
        
        <div class="oferte_content">
        	<div class="top_divider"><img src="/<?php echo base_url(); ?>images/header_divider.png" alt="" title="" width="1" height="164" /></div>
        	<div class="oferta">
            
           		<div class="oferta_content">
                	<img src="/<?php echo base_url(); ?>images/laptop.png" width="94" height="92" border="0" class="oferta_img" />
                	
                    <div class="oferta_details">
                            <div class="oferta_title">Samsung GX 2004 LM</div>
                            <div class="oferta_text">
                            Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco
                            </div>
                            <a href="details.html" class="details">details</a>
                    </div>
                </div>
                <div class="oferta_pagination">
                
                     <span class="current">1</span>
                     <a href="#?page=2">2</a>
                     <a href="#?page=3">3</a>
                     <a href="#?page=3">4</a>
                     <a href="#?page=3">5</a>  
                             
                </div>        

            </div>
            <div class="top_divider"><img src="/<?php echo base_url(); ?>images/header_divider.png" alt="" title="" width="1" height="164" /></div>
        	
        </div> <!-- end of oferte_content-->
        

    </div>
    
   <div id="main_content"> 
   
            <div id="menu_tab">
            <div class="left_menu_corner"></div>
                    <ul class="menu">
                         <li><a href="/<?php echo base_url(); ?>index.php/CHome/index" class="nav1">  Home </a></li>
                         <li class="divider"></li>
                         <li><a href="/<?php echo base_url(); ?>index.php/Ctest" class="nav2">Products</a></li>
                         <li class="divider"></li>
                         <li><a href="CHome" class="nav3">Specials</a></li>
                         <li class="divider"></li>
                         <li><a href="CHome" class="nav4">My account</a></li>
                         <li class="divider"></li>
                         <li><a href="CHome" class="nav4">Sign Up</a></li>
                         <li class="divider"></li>                         
                         <li><a href="CHome" class="nav5">Shipping </a></li>
                         <li class="divider"></li>
                         <li><a href="/<?php echo base_url(); ?>index.php/CContact" class="nav6">Contact Us</a></li>
                         <li class="divider"></li>
                         <li class="currencies">Currencies
                         <select>
                         <option>US Dollar</option>
                         <option>Euro</option>
                         </select>
                         </li>
                    </ul>

             <div class="right_menu_corner"></div>
            </div><!-- end of menu tab -->
            
     <div class="crumb_navigation">
    Navigation: <a href="index.html">Home</a> &lt; <span class="current">Category name</span>
    
    </div>              
    
    
   <div class="left_content">
    <div class="title_box">Categories</div>
    
        <ul class="left_menu">
        <li class="odd"><a href="services.html">Processors</a></li>
        <li class="even"><a href="services.html">Motherboards</a></li>
         <li class="odd"><a href="services.html">Desktops</a></li>
        <li class="even"><a href="services.html">Laptops &amp; Notebooks</a></li>
         <li class="odd"><a href="services.html">Processors</a></li>
         <li class="even"><a href="services.html">Motherboards</a></li>
        <li class="odd"><a href="services.html">Processors</a></li>
        <li class="even"><a href="services.html">Motherboards</a></li>
         <li class="odd"><a href="services.html">Desktops</a></li>
        <li class="even"><a href="services.html">Laptops &amp; Notebooks</a></li>
         <li class="odd"><a href="services.html">Processors</a></li>
         <li class="even"><a href="services.html">Motherboards</a></li>
        </ul> 
        
        
     <div class="title_box">Special Products</div>  
     <div class="border_box">
         <div class="product_title"><a href="details.html">Motorola 156 MX-VL</a></div>
         <div class="product_img"><a href="details.html"><img src="/<?php echo base_url(); ?>images/laptop.png" alt="" title="" border="0" /></a></div>
         <div class="prod_price"><span class="reduce">350$</span> <span class="price">270$</span></div>
     </div>  
     
     
     <div class="title_box">Newsletter</div>  
     <div class="border_box">
		<input type="text" name="newsletter" class="newsletter_input" value="your email"/>
        <a href="#" class="join">join</a>
     </div>  
     
     <div class="banner_adds">
     
     <a href="#"><img src="/<?php echo base_url(); ?>images/bann2.jpg" alt="" title="" border="0" /></a>
     </div>    
        
    
   </div><!-- end of left content -->
  

   
   <div class="center_content">
 
    
    
 

 
 
 
 <div class="center_title_bar">Similar products</div>
 
 
      	<div class="prod_box">
        	<div class="top_prod_box"></div>
            <div class="center_prod_box">            
                 <div class="product_title"><a href="details.html">Motorola 156 MX-VL</a></div>
                 <div class="product_img"><a href="details.html"><img src="/<?php echo base_url(); ?>images/laptop.gif" alt="" title="" border="0" /></a></div>
                 <div class="prod_price"><span class="reduce">350$</span> <span class="price">270$</span></div>                        
            </div>
            <div class="bottom_prod_box"></div>             
            <div class="prod_details_tab">
            <a href="#" title="header=[Add to cart] body=[&nbsp;] fade=[on]"><img src="/<?php echo base_url(); ?>images/cart.gif" alt="" title="" border="0" class="left_bt" /></a>
            <a href="#" title="header=[Specials] body=[&nbsp;] fade=[on]"><img src="/<?php echo base_url(); ?>images/favs.gif" alt="" title="" border="0" class="left_bt" /></a>
            <a href="#" title="header=[Gifts] body=[&nbsp;] fade=[on]"><img src="/<?php echo base_url(); ?>images/favorites.gif" alt="" title="" border="0" class="left_bt" /></a>           
            <a href="details.html" class="prod_details">details</a>            
            </div>                     
        </div>
    
    
 
     	<div class="prod_box">
        	<div class="top_prod_box"></div>
            <div class="center_prod_box">            
                 <div class="product_title"><a href="details.html">Iphone Apple</a></div>
                 <div class="product_img"><a href="details.html"><img src="/<?php echo base_url(); ?>images/p4.gif" alt="" title="" border="0" /></a></div>
                 <div class="prod_price"><span class="price">270$</span></div>                        
            </div>
            <div class="bottom_prod_box"></div>             
            <div class="prod_details_tab">
            <a href="#" title="header=[Add to cart] body=[&nbsp;] fade=[on]"><img src="/<?php echo base_url(); ?>images/cart.gif" alt="" title="" border="0" class="left_bt" /></a>
            <a href="#" title="header=[Specials] body=[&nbsp;] fade=[on]"><img src="/<?php echo base_url(); ?>images/favs.gif" alt="" title="" border="0" class="left_bt" /></a>
            <a href="#" title="header=[Gifts] body=[&nbsp;] fade=[on]"><img src="/<?php echo base_url(); ?>images/favorites.gif" alt="" title="" border="0" class="left_bt" /></a>           
            <a href="details.html" class="prod_details">details</a>             
            </div>                     
        </div>
 
     	<div class="prod_box">
        	<div class="top_prod_box"></div>
            <div class="center_prod_box">            
                 <div class="product_title"><a href="details.html">Samsung Webcam</a></div>
                 <div class="product_img"><a href="details.html"><img src="/<?php echo base_url(); ?>images/p5.gif" alt="" title="" border="0" /></a></div>
                 <div class="prod_price"><span class="reduce">350$</span> <span class="price">270$</span></div>                        
            </div>
            <div class="bottom_prod_box"></div>             
            <div class="prod_details_tab">
            <a href="#" title="header=[Add to cart] body=[&nbsp;] fade=[on]"><img src="/<?php echo base_url(); ?>images/cart.gif" alt="" title="" border="0" class="left_bt" /></a>
            <a href="#" title="header=[Specials] body=[&nbsp;] fade=[on]"><img src="/<?php echo base_url(); ?>images/favs.gif" alt="" title="" border="0" class="left_bt" /></a>
            <a href="#" title="header=[Gifts] body=[&nbsp;] fade=[on]"><img src="/<?php echo base_url(); ?>images/favorites.gif" alt="" title="" border="0" class="left_bt" /></a>           
            <a href="details.html" class="prod_details">details</a>            
            </div>                     
        </div> 
 
 
 
 
    
    
   
   </div><!-- end of center content -->
   
   <div class="right_content">
   		<div class="shopping_cart">
        	<div class="cart_title">Shopping cart</div>
            
            <div class="cart_details">
            3 items <br />
            <span class="border_cart"></span>
            Total: <span class="price">350$</span>
            </div>
            
            <div class="cart_icon"><a href="#" title="header=[Checkout] body=[&nbsp;] fade=[on]"><img src="/<?php echo base_url(); ?>images/shoppingcart.png" alt="" title="" width="48" height="48" border="0" /></a></div>
        
        </div>
   
   
     <div class="title_box">What’s new</div>  
     <div class="border_box">
         <div class="product_title">Motorola 156 MX-VL</div>
         <div class="product_img"><a href="#"><img src="/<?php echo base_url(); ?>images/p2.gif" alt="" title="" border="0" /></a></div>
         <div class="prod_price"><span class="reduce">350$</span> <span class="price">270$</span></div>
     </div>  
     
     
     
    <div class="title_box">Manufacturers</div>
    
        <ul class="left_menu">
        <li class="odd"><a href="services.html">Sony</a></li>
        <li class="even"><a href="services.html">Samsung</a></li>
         <li class="odd"><a href="services.html">Daewoo</a></li>
        <li class="even"><a href="services.html">LG</a></li>
         <li class="odd"><a href="services.html">Fujitsu Siemens</a></li>
         <li class="even"><a href="services.html">Motorola</a></li>
        <li class="odd"><a href="services.html">Phillips</a></li>
        <li class="even"><a href="services.html">Beko</a></li>
        </ul>      
     
     <div class="banner_adds">
     
     <a href="#"><img src="/<?php echo base_url(); ?>images/bann1.jpg" alt="" title="" border="0" /></a>
     </div>        
     
   </div><!-- end of right content -->   
   
            
   </div><!-- end of main content -->
   
   
   
   <div class="footer">
   

        <div class="left_footer">
        <img src="/<?php echo base_url(); ?>images/footer_logo.png" alt="" title="" width="170" height="49"/>
        </div>
        
        <div class="center_footer">
        Template name. All Rights Reserved 2008<br />
          <a href="http://csscreme.com/freecsstemplates/" title="free templates"><img src="/<?php echo base_url(); ?>images/csscreme.jpg" alt="free templates" title="free templates" border="0" /></a><br />
        <img src="/<?php echo base_url(); ?>images/payment.gif" alt="" title="" />
        </div>
        
        <div class="right_footer">
        <a href="index.html">home</a>
        <a href="services.html">about</a>
        <a href="services.html">sitemap</a>
        <a href="services.html">rss</a>
        <a href="contact.html">contact us</a>
        </div>   
   
   </div>                 


</div>
<!-- end of main_container -->
</body>
</html>
