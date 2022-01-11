package MainServer.GameRoom;

import MainServer.Utils;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameRoomRepo {
    static Space s;
    Space conns;

    public GameRoomRepo() {
        this.s = new SequentialSpace();
        this.conns = new SequentialSpace();
    }

    private void insertGameRoom(GameRoom gr) throws InterruptedException {
        s.put(gr.userHostUUID, gr.UUID, gr.number, gr.timestamp);
    }

    private GameRoom getGameRoom(String uuid) throws InterruptedException {
        var q = s.getp(new FormalField(String.class), new ActualField(uuid), new FormalField(Integer.class), new FormalField(Double.class));
        return new GameRoom(q);
    }

    private static GameRoom queryGameRoom(String uuid) throws InterruptedException {
        var q = s.queryp(new FormalField(String.class), new ActualField(uuid), new FormalField(Integer.class), new FormalField(Double.class));
        return new GameRoom(q);
    }

    /**
     * @param userHostUUID User UUID of user that created the room.
     *                 This user will become host of the room.
     * @return UUID of room, used for connecting to room.
     */
    public String create(String userHostUUID) throws InterruptedException {
        GameRoom gr = new GameRoom(
                userHostUUID,
                Utils.createUUID(),
                getNewNumber()
        );
        insertGameRoom(gr);
        conns.put(userHostUUID, gr.UUID);
        return gr.UUID;
    }

    public void changeHost(String uuid, String newHost) throws InterruptedException {
        GameRoom gr = getGameRoom(uuid);
        gr.userHostUUID = newHost;
        insertGameRoom(gr);
    }

    public static boolean isHost(String uuid, String userHostUUID) throws InterruptedException {
        GameRoom gr = queryGameRoom(uuid);

        return gr.userHostUUID.equals(userHostUUID);
    }

    public void addConnection(String userUUID, String roomUUID) throws InterruptedException {
        conns.put(userUUID, roomUUID);
    }

    public void removeConnection(String userHostUUID, String uuid) throws InterruptedException {
        conns.getp(new ActualField(userHostUUID), new ActualField(uuid));
        var curConns = queryConnections(uuid);
        if (curConns.isEmpty()) // no more connections, close room
            close(uuid);
        else if (isHost(uuid, userHostUUID)) // host has left, reassign role
            changeHost(uuid, curConns.get(0));
    }

    public List<String> queryConnections(String uuid) throws InterruptedException {
        var allConns = conns.queryAll( new FormalField(String.class), new ActualField(uuid));
        return allConns.stream().map(o -> (String)(o[0])).collect(Collectors.toList());
    }

    public void close(String uuid) throws InterruptedException {
        getGameRoom(uuid); // remove room uuid from s
        conns.getAll(new FormalField(String.class), new ActualField(uuid)); // remove conns to room from conns
    }

    public boolean exists(String UUID) throws InterruptedException {
        Object[] q = s.queryp(new FormalField(String.class),
                new ActualField(UUID),
                new FormalField(Integer.class),
                new FormalField(Double.class));
        return q!= null;
    }

    public void queryAllRooms() throws InterruptedException {
        var allRooms = s.queryAll(new FormalField(String.class),
                new FormalField(String.class),
                new FormalField(Integer.class),
                new FormalField(Double.class)
        );

        for (var q : allRooms) {
            GameRoom gr = new GameRoom(q);
            System.out.println(gr);
            System.out.println("  >> Connections:");
            ArrayList<String> allConns = (ArrayList<String>) queryConnections(gr.UUID);
            for (var c : allConns)
                System.out.println("    >> " + c);
        }
    }

    private int getNewNumber() throws InterruptedException {
        var allRooms = s.queryAll(new FormalField(String.class),
                new FormalField(String.class),
                new FormalField(Integer.class),
                new FormalField(Double.class)
        );

        ArrayList<Integer> numbers = new ArrayList();
        for (var q : allRooms) {
            numbers.add((int) q[2]);
        }

        for (int i = 1; i <= s.size(); i++) {
            if (!numbers.contains(i)) {
                return i;
            }
        }
        return s.size()+1;
    }

    public String getUUID(String roomName) throws Exception {
        var allRooms = s.queryAll(new FormalField(String.class),
                new FormalField(String.class),
                new FormalField(Integer.class),
                new FormalField(Double.class)
        );

        for (var q : allRooms) {
            if (roomName.equals("room " + q[2])) {
                return (String) q[1];
            }
        }
        throw new Exception("Room with this name does not exist");
    }

}
