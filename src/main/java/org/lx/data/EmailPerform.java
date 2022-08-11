package org.lx.data;

public class EmailPerform {
	
	private String type;
	
	private Double online=(double) 0;
	
	private Double delay=(double) 0;
	
	private Double validate=(double) 0;
	
	private Double allCount=(double) 0;
	
	private Double valiCount=(double) 0;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getOnline() {
		return online;
	}

	public void setOnline(Double online) {
		this.online = online;
	}

	public Double getDelay() {
		return delay;
	}

	public void setDelay(Double delay) {
		this.delay = delay;
	}

	public Double getValidate() {
		return validate;
	}

	public void setValidate(Double validate) {
		this.validate = validate;
	}

	public Double getAllCount() {
		return allCount;
	}

	public void setAllCount(Double allCount) {
		this.allCount = allCount;
	}

	public Double getValiCount() {
		return valiCount;
	}

	public void setValiCount(Double valiCount) {
		this.valiCount = valiCount;
	}
	

}
