<?php if(false){?>
<html>
<body>
<?php 
}

$wImageUrl = base_url()."app_resources/images/_items/".$id.".jpg";

$wDetailUrl =  base_url()."index.php/CHome/showDetails/".$id;

$wName = $name;
if (strlen($wName)>25){
	$wName = substr($wName, 0, 23) . " ...";
}

?>

	<div class="border_box">
		<div class="product_title">
			<a href="<?php echo $wDetailUrl; ?>"><?php echo $id;?></a>
		</div>
		<div class="product_img">
			<a href="<?php echo $wDetailUrl; ?>"><img
				src="<?php echo $wImageUrl; ?>"
				alt="" title="" border="0" width="100" /> </a>
		</div>
		<?php echo $wName;?>
		<div class="prod_price">
			<span class="price"><?php echo $price; ?> EUR</span>
			&nbsp; 
			<span class="stock"  title="<?php echo $stockQualityClass; ?>">Stock: <span class="stock<?php echo $stockQualityClass; ?>" ><?php echo $stock; ?></span></span>
			
		</div>
	</div>
	
	
	
	

<?php if(false){?>
</body>
</html>
<?php }?>