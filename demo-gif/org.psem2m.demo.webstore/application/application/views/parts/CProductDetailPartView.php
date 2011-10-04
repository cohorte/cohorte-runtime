<?php if(false){?>
<html>
<body>
<?php
}

$wImageUrl = "/".base_url()."app_resources/images/_items/".$Item['id'].".png";
?>


	<div class="prod_box_big">
		<div class="top_prod_box_big"></div>
		<div class="center_prod_box_big">

			<div class="product_img_big">
				<a href="javascript:popImage('<?php echo $wImageUrl; ?>','Some Title')"
					title="header=[Zoom] body=[&nbsp;] fade=[on]"><img
					src="<?php echo $wImageUrl; ?>"
					alt="" title="" border="0" width="150px" /> </a>
					
					
				<div class="thumbs">
					<a href="" title="header=[Thumb1] body=[&nbsp;] fade=[on]"><img
						src="<?php echo $wImageUrl; ?>"
						alt="" title="" border="0" width="25px" /> </a> 
				</div>
			</div>
			<div class="details_big_box">
				<div class="product_title_big"><?php echo $Item['id']; ?> </div>
				<div class="specifications">
					Product : <span class="blue"><?php echo $Item['lib']; ?></span>
					<br />
					Description: <span class="blue"><?php echo $Item['text']; ?></span>
					<br />
					Availability: <span class="prod_price_big"> <span class="stock<?php echo $Item['stockclass']; ?>"><?php echo $Item['stock']; ?></span></span>
					<br />
					Tip transport: <span class="blue">Mic</span>
					<br />
					Tax include: <span class="blue">TVA</span><br />
				</div>
				<div class="prod_price_big"> <span class="price"><?php echo $Item['price']; ?> EUR</span>
				</div>

				<a href="#" class="addtocart">add to cart</a> <a href="#"
					class="compare">compare</a>
			</div>
		</div>
		<div class="bottom_prod_box_big"></div>
	</div>
	
	


<?php if(false){?>
</body>
</html>
<?php }?>