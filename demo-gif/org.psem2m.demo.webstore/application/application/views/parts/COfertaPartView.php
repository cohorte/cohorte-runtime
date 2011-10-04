<?php if(false){?>
<html>
<body>
<?php 
}

$wRandomIdx = rand(1, 21);
$wRandomIdx = str_pad($wRandomIdx, 3, "0", STR_PAD_LEFT);
$wRandomTyp = rand(0, 1);

$wRandomItem = (($wRandomTyp==0)?'screen':'mouse').$wRandomIdx;

$wRandomImage= '_items/'.$wRandomItem.'.png';

$wDetailUrl = "/". base_url()."index.php/CHome/showDetails/".$wRandomItem;

?>
	<div class="oferte_content">
		<div class="top_divider">
			<img
				src="/<?php echo base_url(); ?>app_resources/images/header_divider.png"
				alt="" title="" width="1" height="164" />
		</div>
		<div class="oferta">

			<div class="oferta_content">
				<a href="<?php echo $wDetailUrl;?>" > <img src="/<?php echo base_url(); ?>app_resources/images/<?php echo $wRandomImage;?>"
					width="94" height="92" border="0" class="oferta_img" /></a>

				<div class="oferta_details">
					<div class="oferta_title"><?php echo $wRandomItem;?></div>
					<div class="oferta_text">Lorem ipsum dolor sit amet, consectetur
						adipisicing elit, sed do eiusmod tempor incididunt ut labore et
						dolore magna aliqua. Ut enim ad minim veniam, quis nostrud
						exercitation ullamco</div>
					<a href="<?php echo $wDetailUrl;?>" class="details">details</a>
				</div>
			</div>
			<div class="oferta_pagination">

				<span class="current">1</span> <a href=".">2</a> <a
					href=".">3</a> <a href=".">4</a> <a href=".">5</a>

			</div>

		</div>
		<div class="top_divider">
			<img
				src="/<?php echo base_url(); ?>app_resources/images/header_divider.png"
				alt="" title="" width="1" height="164" />
		</div>

	</div>

	

<?php if(false){?>
</body>
</html>
<?php }?>