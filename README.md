# Shutterfly
Shutterfly Customer Lifetime Value Code-Challenge

Assumptions

1) Average customer lifespan for every customer is 10 years;
2) I am assuming while updating customer we are just updating last_name, adr_city, adr_state and not event_time because if we update it we'll lose the customer creation date which is necessary for LTV calculation.
3) I am assuming while updating orders we are just updating event_time, total_amount and not customer_id  as it does not make sense updating a customer id corresponding to an order.
