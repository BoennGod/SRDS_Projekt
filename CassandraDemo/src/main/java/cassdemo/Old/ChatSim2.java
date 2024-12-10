package cassdemo;
import com.datastax.driver.core.PreparedStatement;
import cassdemo.backend.BackendSession;
import cassdemo.backend.BackendException;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Random;


import com.datastax.driver.core.Row;

public class ChatSim implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(ChatSim.class);
    BackendSession session;
    String id;
    ArrayList<String> nicks = new ArrayList<String>();
    Random rand = new Random();
    public ChatSim(String id,BackendSession session){
        this.id = id;
        this.session = session;
    }

    public void fillMockList(){
        nicks.add("Patryk");
        nicks.add("Natalia");
        nicks.add("Mateusz");
        nicks.add("Borys");
    }

    public void setNickname()  throws BackendException {
        int randomIndex = rand.nextInt(nicks.size());
        String nick = nicks.get(randomIndex);
        if(session.checkIfNickIsFree(nick)){
            session.upsertNick(nick, id);
        }
        if(session.checkIfNickIsFree(nick)) {
            logger.info("Dodano nick "+nick);
        }
        session.deleteNick(nick);

    }

    @Override
    public void run() {
        fillMockList();
        try {
            setNickname();
        }
        catch ( BackendException e) {
            throw new RuntimeException(e);
        }

    }


}