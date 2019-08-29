package de.brkn.pivse.domain;

public class AmazonItem {

	private int id;
	private String asin;
	private String title;
	private String detailPageUrl;
	private int salesRank;
	private String smallImage;
	private String mediumImage;
	private String largeImage;
	private String browseNodeId;
	private int audienceRating;
	private boolean ov;
	private String releaseDate;

	public AmazonItem(int id, String asin) {
		this.id = id;
		this.asin = asin;
	}

	public int getId() {
		return id;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public String getAsin() {
		return asin;
	}

	public String getDetailPageUrl() {
		return detailPageUrl;
	}

	public void setDetailPageUrl(String detailPageUrl) {
		this.detailPageUrl = detailPageUrl;
	}

	public int getSalesRank() {
		return salesRank;
	}

	public void setSalesRank(int salesRank) {
		this.salesRank = salesRank;
	}

	public String getSmallImage() {
		return smallImage;
	}

	public void setSmallImage(String smallImage) {
		this.smallImage = smallImage;
	}

	public String getMediumImage() {
		return mediumImage;
	}

	public void setMediumImage(String mediumImage) {
		this.mediumImage = mediumImage;
	}

	public String getLargeImage() {
		return largeImage;
	}

	public void setLargeImage(String largeImage) {
		this.largeImage = largeImage;
	}

	public String getBrowseNodeId() {
		return browseNodeId;
	}

	public void setBrowseNodeId(String browseNodeId) {
		this.browseNodeId = browseNodeId;
	}

	public int getAudienceRating() {
		return audienceRating;
	}

	public void setAudienceRating(int audienceRating) {
		this.audienceRating = audienceRating;
	}

	public boolean isOv() {
		return ov;
	}

	public void setOv(boolean ov) {
		this.ov = ov;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

}
