package Coding_Challenge;

public class Site_Visit {
	
	private String type;
	private String verb;
	private String key;
	private String event_time;
	private String customer_id;
	private tags[] tag;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setTag(tags[] tag) {
		this.tag = tag;
	}
	public tags[] getTag() {
		return tag;
	}
	public String getVerb() {
		return verb;
	}
	public void setVerb(String verb) {
		this.verb = verb;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getEvent_time() {
		return event_time;
	}
	public void setEvent_time(String event_time) {
		this.event_time = event_time;
	}
	public String getCustomer_id() {
		return customer_id;
	}
	public void setCustomer_id(String customer_id) {
		this.customer_id = customer_id;
	}
}
