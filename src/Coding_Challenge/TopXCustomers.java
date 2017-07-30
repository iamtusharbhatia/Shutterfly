package Coding_Challenge;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class TopXCustomers {

	//In-memory data structures to hold all of the information
	private Map<String,Customer> customers = new HashMap<String,Customer>();
	private Map<String,Site_Visit> visits = new HashMap<String, Site_Visit>();
	private List<Image> images = new ArrayList<>();
	private Map<String,List<Order>> orders = new HashMap<String, List<Order>>(); 

	private LocalDate timeFrameEndDate = LocalDate.parse("0001-01-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	private Map<String,Double> customerLTV = new HashMap<String,Double>();

	//Additional list to maintain the unordered ingest operations to be inserted later
	private List<JsonObject> unorderedOperations = new ArrayList<JsonObject>();
	private static final Gson gson = new Gson();




	public void addUpdateCustomer(JsonObject e) {
		String custId = e.get("key").getAsString();
		String verb = e.get("verb").getAsString();
		String event_time = e.get("event_time").getAsString();

		if(custId != null && !custId.equals("") && verb != null && !verb.equals("") &&
				event_time != null && !event_time.equals("")){

			if(verb.equals("NEW")){
				customers.put(custId, gson.fromJson(e, Customer.class));
			} else {
				//This else block updates the customer details like last_name, adr_city, adr_city and doesn't update the 
				//event_time otherwise we'll lose the time when the customer was created which is required to calculate LTV
				if(customers.containsKey(custId)){
					Customer cust = customers.get(custId);

					String last_name = e.get("last_name").getAsString();
					String adr_city = e.get("adr_city").getAsString();
					String adr_state = e.get("adr_state").getAsString();

					if(last_name != null){
						cust.setLast_name(last_name);
					}
					if(adr_city != null){
						cust.setAdr_city(adr_city);
					}
					if(adr_state != null){
						cust.setAdr_state(adr_state);
					}
				} else {
					unorderedOperations.add(e);
				}
			}
		}
	}

	public void addNewSiteVisit(JsonObject e){
		String key = e.get("key").getAsString();
		String custId = e.get("customer_id").getAsString();
		String verb = e.get("verb").getAsString();
		String event_time = e.get("event_time").getAsString();

		if(custId != null && !custId.equals("") && verb != null && !verb.equals("") &&
				event_time != null && !event_time.equals("") && key != null && !key.equals("")){

			//It will only insert value in visits data structure if there is already an entry for that customer in the
			//customer table.
			if(customers.containsKey(custId)){
				visits.put(custId,gson.fromJson(e, Site_Visit.class));
			} else {
				unorderedOperations.add(e);
			}
		}
	}

	public void addImage(JsonObject e){
		String key = e.get("key").getAsString();
		String custId = e.get("customer_id").getAsString();
		String verb = e.get("verb").getAsString();
		String event_time = e.get("event_time").getAsString();

		if(custId != null && !custId.equals("") && verb != null && !verb.equals("") &&
				event_time != null && !event_time.equals("") && key != null && !key.equals("")){

			//It will only insert value in images data structure if there is already an entry for that customer in the
			//customer table.
			if(customers.containsKey(custId)){
				images.add(gson.fromJson(e, Image.class));
			} else {
				unorderedOperations.add(e);
			}
		}
	}

	public void addUpdateOrder(JsonObject e){
		String key = e.get("key").getAsString();
		String custId = e.get("customer_id").getAsString();
		String verb = e.get("verb").getAsString();
		String event_time = e.get("event_time").getAsString();
		String total_amount = e.get("total_amount").getAsString();


		if(custId != null && !custId.equals("") && verb != null && !verb.equals("") && 
				event_time != null && !event_time.equals("") && key != null && !key.equals("") && 
				total_amount != null && !total_amount.equals("")){

			//It will only insert/update value in orders data structure if there is already an entry for that customer in the
			//customer table.
			if(customers.containsKey(custId)){

				if(e.get("verb").getAsString().equals("NEW")){

					//Here I am calculating the end date of the time frame provided in the test case
					LocalDate date = toLocalDate(e.get("event_time").getAsString());
					if(date.compareTo(timeFrameEndDate) > 0){
						timeFrameEndDate = date;
					}

					//Now we'll add the new order details to the orders list of the respective customer
					if(orders.containsKey(custId)){
						List<Order> list= orders.get(custId);
						list.add(gson.fromJson(e, Order.class));
						orders.put(custId, list);
					} else {
						List<Order> list = new LinkedList<Order>();
						list.add(gson.fromJson(e, Order.class));
						orders.put(custId, list);
					}
				} else {

					String orderId = e.get("key").getAsString();
					List<Order> list= orders.get(custId);
					boolean flag = false;
					
					if(list != null){
						for(Order o: list){
							if(orderId.equals(o.getKey())){
								o.setEvent_time(event_time);
								o.setTotal_amount(total_amount );
								flag = true;
							}
						}
						if(flag == false){
							unorderedOperations.add(e);
						}
					} else {
						unorderedOperations.add(e);
					}
				}
			} else {
				unorderedOperations.add(e);
			}
		}
	}

	public LocalDate toLocalDate(String date){

		String str = date.substring(0, 10);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate localDate = LocalDate.parse(str, formatter);

		return localDate;
	}


	public void ingest(JsonObject e){

		String type = e.get("type").getAsString();

		//Here we redirect the insertion to the respective function and if there any wrong type comes in input then
		//it would be ignored.
		if(type.equals("CUSTOMER")){
			addUpdateCustomer(e);
		} else if (type.equals("SITE_VISIT")){
			addNewSiteVisit(e);
		} else if (type.equals("IMAGE")){
			addImage(e);
		} else if (type.equals("ORDER")){
			addUpdateOrder(e);
		}
	}


	public void calculateLTV(String id) {

		Double ltv = 0.0;
		Double totalSpending = 0.0;
		Double spendingPerWeek = 0.0;

		//Here I fetch the date when the customer was created
		Customer cust = customers.get(id);
		LocalDate startDate = toLocalDate(cust.getEvent_time());

		//Here I calculate time frame for a particular customer by subtracting the timeFrameEndDate of test case with
		//the customer's creation date.
		long daysBetween = ChronoUnit.DAYS.between(startDate, timeFrameEndDate);
		
		if(daysBetween < 0 ){
			customerLTV.put(id, 0.0);
		} else {
			List<Order> orderList = orders.get(id);
			if(orderList != null){
				for(Order o: orderList){
					String amt = o.getTotal_amount();
					amt = amt.split(" ")[0];
					totalSpending = totalSpending + Double.parseDouble(amt);
				}
			}
			
			//Here I am calculating spending per week by the customer
			spendingPerWeek = (totalSpending / daysBetween)*7;

			ltv = 52*(spendingPerWeek) * 10;
			customerLTV.put(id, ltv);
		}
	}

	private PriorityQueue<Entry<String,Double>> ltvQueue = new PriorityQueue<>(new MyComparator());

	public class MyComparator implements Comparator<Entry<String,Double>> {

		@Override
		public int compare(Entry<String,Double> e1, Entry<String,Double> e2) {
			if(e1.getValue()  > e2.getValue()){
				return -1;
			} else if (e1.getValue()  < e2.getValue()){
				return 1;
			} else {
				return 0;
			}
		}
	}


	public void topXSimpleLTVCustomers(int x, JsonArray Data) throws IOException{

		//This while loop ingests data corresponding to all the events into the In-memory data structure.
		Iterator<JsonElement> iterator = Data.iterator();
		while(iterator.hasNext()){
			JsonObject jsonObj = (JsonObject)iterator.next();
			ingest(jsonObj);
		}

		//This loop tries to again ingest the data which earlier came in an unordered way.
		//Like If order update comes before the create event for that customer.
		//So the initial while loop will skip the order insert because customer hasn't been created.
		//And then finally when first while loop is completed then this addition loop will insert those unordered operations.
		int count = 2;
		while(count > 0){
			List<JsonObject> tempUnorderedOperations = new ArrayList<JsonObject>(unorderedOperations);
			unorderedOperations.clear();
			
			if(tempUnorderedOperations.size() > 0){
				for(JsonObject e: tempUnorderedOperations){
					ingest(e);
				}
			} else {
				break;
			}
			count--;
		}
		//This for loop calculates customerLTV for each customer and adds it to a HashMap
		for(Entry<String,Customer> obj: customers.entrySet()){
			calculateLTV(obj.getKey());
		}

		//Moving the contents from the customerLTV hashMap to a priority queue where our customers will
		// be sorted according to their LTV.
		for(Entry<String,Double> obj: customerLTV.entrySet()){
			ltvQueue.add(obj);
		}

		//Now writing the topXCustomerLTV to the output file
		File fout = new File("output");
		FileOutputStream fos = new FileOutputStream(fout);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
		int size = 0;
		if(x < ltvQueue.size()){
			size = x;
		} else {
			size = ltvQueue.size();
		}
		
		for(int i=0; i<size; i++){
			Entry<String, Double> entry = ltvQueue.remove();
			bw.write("Customer ID: " + entry.getKey() + " Lifetime Value: " + entry.getValue());
			bw.newLine();
		}
		bw.close();
	}

	//This program will return a Map where the key will be the customer id and the value will be revenuePerVisit for
	//that customer
	public Map<String, Double> revenuePerVisits(){

		//This Map has customer ID as the key and number of times it visited the site as its value.
		Map<String, Integer> numOfVisits = new HashMap<String, Integer>();
		for(Entry<String,Customer> obj: customers.entrySet()){

			if(visits.containsKey(obj.getKey())){
				if(numOfVisits.containsKey(obj.getKey())){
					numOfVisits.put(obj.getKey(), numOfVisits.get(obj.getKey()) + 1);
				} else {
					numOfVisits.put(obj.getKey(), 1);
				}
			} else {
				numOfVisits.put(obj.getKey(), 0);
			}
		}

		//This Map has customer ID as the key and total revenue for that customer as its value.
		Map<String, Double> totalRevenuePerCust = new HashMap<String, Double>();
		for(Entry<String,Customer> obj: customers.entrySet()){
			Double totalSpending = 0.0;

			if(orders.containsKey(obj.getKey())) {	
				List<Order> orderList = orders.get(obj.getKey());
				for(Order o: orderList){
					String amt = o.getTotal_amount();
					amt = amt.split(" ")[0];
					totalSpending = totalSpending + Double.parseDouble(amt);
				}
				totalRevenuePerCust.put(obj.getKey(), totalSpending);
			} else {
				totalRevenuePerCust.put(obj.getKey(), 0.0);
			}
		}

		//This final Map has customer ID as the key and total revenue per visit as its value.
		Map<String, Double> totalRevenuePerVisit = new HashMap<String, Double>();
		for(Entry<String,Double> obj: totalRevenuePerCust.entrySet()){

			int numOfvisits = numOfVisits.get(obj.getKey());
			if(numOfvisits > 0){
				totalRevenuePerVisit.put(obj.getKey(), obj.getValue() / numOfVisits.get(obj.getKey()));
			} else {
				totalRevenuePerVisit.put(obj.getKey(), 0.0);
			}
		}

		return totalRevenuePerVisit;
	}



	//This program will return a Map where the key will be the customer id and the value will be visitsPerWeek for
	//that customer
	public Map<String, Double> visitsPerWeek(){

		//This Map has customer ID as the key and number of times it visited the site as its value.
		Map<String, Integer> numOfVisits = new HashMap<String, Integer>();
		for(Entry<String,Customer> obj: customers.entrySet()){

			if(visits.containsKey(obj.getKey())){
				if(numOfVisits.containsKey(obj.getKey())){
					numOfVisits.put(obj.getKey(), numOfVisits.get(obj.getKey()) + 1);
				} else {
					numOfVisits.put(obj.getKey(), 1);
				}
			} else {
				numOfVisits.put(obj.getKey(), 0);
			}
		}

		//This Map has customer ID as the key and number of timeframe days as its value.
		Map<String, Double> numOfDays = new HashMap<String, Double>();
		for(Entry<String,Customer> obj: customers.entrySet()){
			//Here I fetch the date when the customer was created
			Customer cust = customers.get(obj.getKey());
			LocalDate startDate = toLocalDate(cust.getEvent_time());

			//Here I calculate time frame for a particular customer by subtracting the timeFrameEndDate of test case with
			//the customer's creation date.
			long daysBetween = ChronoUnit.DAYS.between(startDate, timeFrameEndDate);
			if(daysBetween < 0 ){
				daysBetween = 0;
			}

			numOfDays.put(obj.getKey(), (double)daysBetween);
		}

		//This final Map has customer ID as the key and total revenue per visit as its value.
		Map<String, Double> visitsPerWeekPerCust = new HashMap<String, Double>();
		for(Entry<String,Double> obj: numOfDays.entrySet()){

			int numOfvisitsForCust = numOfVisits.get(obj.getKey());
			if(numOfvisitsForCust > 0){
				visitsPerWeekPerCust.put(obj.getKey(), numOfVisits.get(obj.getKey())* 7 / obj.getValue());
			} else {
				visitsPerWeekPerCust.put(obj.getKey(), 0.0);
			}
		}
		return visitsPerWeekPerCust;
	}



	public static void main(String[] args) {

		TopXCustomers topCust = new TopXCustomers();

		try {

			//Parsing the input file here		
			BufferedReader br = new BufferedReader(new FileReader("input"));
			JsonArray jsonArray = gson.fromJson(br, JsonArray.class);

			//This method will tell us the top X Life time Value customers
			topCust.topXSimpleLTVCustomers(3, jsonArray);

			//Function to retrieve revenue / visit for all the customers
			System.out.println("Revenue per visit for all the customers \n" + topCust.revenuePerVisits());

			//Function to retrieve visits / week for all the customers
			System.out.println("\nVisits per week for all the customers \n" + topCust.visitsPerWeek());

		} catch(FileNotFoundException fe) {
			fe.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
