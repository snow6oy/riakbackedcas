package uk.co.nascency.riakbackedcas.registry;

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.IRiakObject;
import com.basho.riak.client.RiakException;
import com.basho.riak.client.RiakRetryFailedException;
import com.basho.riak.client.bucket.Bucket;
import org.apache.commons.lang.SerializationUtils;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.AbstractDistributedTicketRegistry;
import org.jasig.cas.ticket.registry.AbstractTicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.basho.riak.client.RiakFactory.pbcClient;

public class RiakBackedTicketRegistry extends AbstractDistributedTicketRegistry {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private IRiakClient riakClient;
    private Bucket casBucket;

    public RiakBackedTicketRegistry() {
        try {
            logger.info("Connecting to RIAK");
            riakClient = pbcClient();
        } catch (RiakException e) {
            e.printStackTrace();
        }
        try {
            casBucket = riakClient.createBucket("cas").execute();
        } catch (RiakRetryFailedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void updateTicket(Ticket ticket) {
        try {
            logger.info("Updating TICKET " + ticket.getId() + " from RIAK");
            casBucket.<byte[]>store(ticket.getId(), SerializationUtils.serialize(ticket)).execute();

        } catch (RiakRetryFailedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean needsCallback() {
        return false;
    }

    @Override
    public void addTicket(Ticket ticket) {
        try {
            logger.info("Saving TICKET " + ticket.getId() + " to RIAK");
            casBucket.<byte[]>store(ticket.getId(), SerializationUtils.serialize(ticket)).execute();
        } catch (RiakRetryFailedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Ticket getTicket(String ticketId) {
        try {
            logger.info("Getting TICKET " + ticketId + " from RIAK");
            IRiakObject list =  casBucket.fetch(ticketId).execute();
            return (Ticket) SerializationUtils.deserialize(list.getValue());

        } catch (RiakRetryFailedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean deleteTicket(String ticketId) {
        try {
            casBucket.delete(ticketId).execute();
            return true;
        } catch (RiakException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Collection<Ticket> getTickets() {
        List<String> list = null;
        List<Ticket> ticketList = new ArrayList<>();
        try {
            list = casBucket.keys().getAll();
            for (String ticketId : list) {
                byte[] byteArray =  casBucket.fetch(ticketId, byte[].class).execute();
               Ticket ticket = (Ticket) SerializationUtils.deserialize(byteArray);
                ticketList.add(ticket);
            }
            return ticketList;
        } catch (RiakException e) {
            e.printStackTrace();
        }
        return null;
    }
}
