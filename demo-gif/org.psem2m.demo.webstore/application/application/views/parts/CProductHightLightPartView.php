<?php if(false){?>
<html>
<body>
<?php 
}

$wImageUrl = "/".base_url()."app_resources/images/_items/".$Item['id'].".jpg";

$wDetailUrl = "/". base_url()."/index.php/CHome/showDetails/".$Item['id'];

?>

	<div class="border_box">
		<div class="product_title">
			<a href="<?php echo $wDetailUrl; ?>"><?php echo $Item['name'];?></a>
		</div>
		<div class="product_img">
			<a href="<?php echo $wDetailUrl; ?>"><img
				src="<?php echo $wImageUrl; ?>"
				alt="" title="" border="0" width="100" /> </a>
		</div>
		<div class="prod_price">
			<span class="price"><?php echo $Item['price']; ?> EUR</span>
			&nbsp; 
			<span class="stock">Availability: <span class="stock<?php echo $Item['qualityClass']; ?>"><?php echo $Item['stock']; ?></span></span>
			
		</div>
	</div>
	
	
	
	

<?php if(false){?>
</body>
</html>
<?php }?>