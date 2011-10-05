<?php if(false){?>
<html>
<body>
<?php }



//Displays the total number of items in the cart.
$wNbItems = $this->cart->total_items();

//Displays the total amount in the cart.
$wTotal = $this->cart->total();

?>

<div class="shopping_cart">


<div class="cart_title"><a href="/<?php echo base_url(); ?>index.php/CShoppingCart" title="Checkout" >Shopping cart</a></div>

<div class="cart_details">
<?php echo $wNbItems; ?> items
<span class="border_cart"></span> 
Total: <span class="price"><?php echo $wTotal;?> EUR</span>
</div>

<div class="cart_icon">
<a href="/<?php echo base_url(); ?>index.php/CShoppingCart" title="Checkout">
<img src="/<?php echo base_url(); ?>app_resources/images/shoppingcart.png" alt="" title="Checkout" width="48" height="48" border="0" /> </a>
</div>

</div>

<?php if(false){?>
</body>
</html>
<?php }?>