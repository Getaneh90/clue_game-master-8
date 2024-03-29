import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicArrowButton;


public class BoardPanel extends JPanel {

	final ImageIcon gameboard;
    String value;
    Client client;
    ClientFrame clientFrame;
    Player currentPlayer; //Characters now in Player class. Access specific character by currentPlayer.getCharacter(). ...
    Message messageReceived;
    //final int WEST = 0, EAST = 1, NORTH = 2, SOUTH = 3, ENTER_ROOM = 4;
    final int WEST = 0, EAST = 1, NORTH = 2, SOUTH = 3; 
    final int ENTER_ROOM = 0;
    String btnValues;
    BasicArrowButton[] movementButtons;
    JLabel boardLabel;
    JButton[] enterButton;
    JButton[] roomButtons; //to hold buttons in room
    JButton btnRollDice;
    int dice_roll_value = 0;
    int movementAmount = 0; 
    char[] cArray_movement_enter;
    int xC = 0; //x coordinate for drawing on board
    int yC = 0; //y coordinate for drawing on board
    int currentXgrid = 0; //x coordinate location for tile grid. this x coord is sent to server
    int currentYgrid = 0; //y coordinate location for tile grid. this y coord is sent to server

    public BoardPanel(Client clientConnection, ClientFrame clientFrame, Player player) {
        client = clientConnection;
        this.clientFrame = clientFrame;
        currentPlayer = player;
        
        initComponents(); //init all but board 

        boardLabel = new JLabel("");
        gameboard = new ImageIcon(getClass().getResource("resources/board.jpg"));
        int w = gameboard.getIconWidth();
        int h = gameboard.getIconHeight();
        setPreferredSize(new Dimension(w, h));
        this.add(boardLabel);
        boardLabel.setBounds(6, 6, 569, 523); 

        cArray_movement_enter = new char[5];

        xC = currentPlayer.getCharacter().getxStarting() * 21;
        yC = currentPlayer.getCharacter().getyStarting() * 20;

        currentXgrid = currentPlayer.getCharacter().getxStarting();
        currentYgrid = currentPlayer.getCharacter().getyStarting();

        //request movement options at launch
        requestBtnsCall(currentXgrid, currentYgrid);
        enableOrdisableBtns(movementButtons, enterButton, cArray_movement_enter);
        
        /*TODO ----- TESTING IF WE CAN RECIEVE PLAYER MAP
        try {
			updatePlayerLocations();
		} catch (ClassNotFoundException | IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} */

        movementButtons[SOUTH].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
              
                    if(movementAmount <=  dice_roll_value){
                      
                        yC += 20;
                        repaint();
                        currentYgrid++;
                        requestBtnsCall(currentXgrid, currentYgrid);
                        enableOrdisableBtns(movementButtons, enterButton, cArray_movement_enter);
                        movementAmount--;
                    }


                else{
                    disableButtons(movementButtons);


                }
           
            }
            
       
        });

        movementButtons[NORTH].addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent e) {
               
               
                    if(movementAmount <=  dice_roll_value){

                    yC -= 20;
                    currentYgrid--;
                    repaint();
                    
                    requestBtnsCall(currentXgrid, currentYgrid);
                    enableOrdisableBtns(movementButtons, enterButton, cArray_movement_enter);
                    movementAmount--;
    
                } else{
                    disableButtons(movementButtons);


                }
           
                    
        
            } 

        });







        movementButtons[EAST].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              
                if(movementAmount <=  dice_roll_value){
                xC += 21;
                repaint();
                currentXgrid++;
                requestBtnsCall(currentXgrid, currentYgrid);
                enableOrdisableBtns(movementButtons, enterButton, cArray_movement_enter);
                movementAmount--;
                    
                 } else{
                    disableButtons(movementButtons);


                }
           
           
           
            }
        });

        movementButtons[WEST].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               
               
                if(movementAmount <=  dice_roll_value){
                xC -= 21;
                repaint();
                currentXgrid--;
                requestBtnsCall(currentXgrid, currentYgrid);
                enableOrdisableBtns(movementButtons, enterButton, cArray_movement_enter);
                movementAmount--; 
                } else{
                    disableButtons(movementButtons);


                }
           
           
           
            }
        });

        enterButton[ENTER_ROOM].addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                //figure out which room
                int roomNumber = getDoorId(currentXgrid, currentYgrid);
                int roomDirection = getDirection(currentXgrid, currentYgrid);
                drawInRoom(roomNumber, roomDirection);
                String rn = String.valueOf(roomNumber);
                String rd = String.valueOf(roomDirection);
                clientFrame.addToLogConsole("room Number: " + rn + "room direction: " + rd);
                requestBtnsCall(currentXgrid, currentYgrid);
                enableOrdisableBtns(movementButtons, enterButton, cArray_movement_enter);
        	}
        });

        btnRollDice.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                try {
                    requestDiceRoll(dice_roll_value, movementAmount);

                } catch (IOException | ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
        	}
        });
    } 

    private void disableButtons(JButton movementButtons[]){

        boolean []moveOptions = {false,false,false,false};

        movementButtons[WEST].setEnabled(false);
    	movementButtons[EAST].setEnabled(false);
    	movementButtons[NORTH].setEnabled(false);
        movementButtons[SOUTH].setEnabled(false);

    }



    private void requestDiceRoll(int dice_roll_value, int movementAmount) throws IOException, ClassNotFoundException
    {
        client.send(new Message(ClueGameConstants.REQUEST_DICE_ROLL, null));
        messageReceived = client.getMessage();
        dice_roll_value =  (int) messageReceived.getData();
        //output value from server
        String str = String.valueOf(dice_roll_value);
        clientFrame.addToLogConsole("Dice roll: " + str); 
        
        movementAmount = dice_roll_value;
        
    }


    private int getDoorId(int row, int col) {
		for(ClueGameConstants.DOORS door : ClueGameConstants.DOORS.values()) {
			if(door.getRow() == row && door.getCol() == col) 
				return door.getRoomNumber();
		}
		return 0; 
    }
    
    private int getDirection(int row, int col) {
		for(ClueGameConstants.DOORS door : ClueGameConstants.DOORS.values()) {
			if(door.getRow() == row && door.getCol() == col) 
				return door.getDirection();
		}
		return 0;
    }

    public void drawInRoom(int roomNumber, int roomDirection)
    {
        //cons:1 ; billiard:2; lib:3; study:4; ball: 5; hall:6; lounge:8; kitchen:9; dining: 10
        //room direction 0=up; 1=down; 2=left; 3=right
        int multiplier = currentPlayer.getCharacter().getTurnOrder();
        if (roomNumber == 1) //conservatory down 2, left turn over num
        {
            yC += 20*2;
            xC += 21;
            currentXgrid++;
            xC -= 21 * multiplier;
            currentYgrid = currentYgrid+2;
            currentXgrid = currentXgrid - multiplier;
        }
        if (roomNumber == 10) //dining room
        {
            if (roomDirection == 1)
            {
                yC += 20*2;
                currentYgrid = currentYgrid+2;
            }
            xC += 21 * multiplier;
            currentXgrid = currentXgrid - multiplier;
        }
        if (roomNumber == 4) //study //up 2, left multiplier
        {
            yC-=20*2;
            xC-= 21* multiplier;
            currentYgrid = currentYgrid-2;
            currentXgrid = currentXgrid - multiplier;
        }
        if (roomNumber == 8) //lounge //right 2, up mult
        {
            yC -= 20*multiplier;
            xC += 21*2;
            currentYgrid = currentYgrid - multiplier;
            currentXgrid = currentXgrid + 2;
        }
        if (roomNumber == 3) //library
        {
            if (roomDirection == 0)
            {
                yC-= 20*2; //up 2
                xC+= 21*3; //right 3
                currentXgrid = currentXgrid+3;
                xC-= 21*multiplier; //left by 6
                currentYgrid = currentYgrid+2;
            }
            else
            {
                xC-= 21*multiplier;
            }
            currentXgrid = currentXgrid - multiplier;
        }
        if (roomNumber == 9) //kitchen
        {
            yC+= 20* multiplier;
            xC+= 21;
            currentYgrid = currentYgrid + multiplier;
            currentXgrid++;
        }
        if (roomNumber == 6) //hall
        {
            if (roomDirection == 0) //up
            {
                yC-= 20* multiplier; //up multiplier
                currentYgrid = currentYgrid - multiplier;
            }
            else //right
            {
                yC+= 20*2;
                currentYgrid = currentYgrid + 2;
                xC+= 21; //right one
                yC-= 20* multiplier;
                currentYgrid = currentYgrid - multiplier;
                currentXgrid++;
            }
        }
        if (roomNumber == 2) //billiard
        {
            if (roomDirection == 2) //left
            {
                yC+= 20;
                xC-= 21*multiplier;
                currentXgrid = currentXgrid - multiplier;
            }
            else //down
            {
                xC-= 21*2;
                currentXgrid= currentXgrid - 2;
                yC+= 20;
                xC+= 21*multiplier;
                currentXgrid = currentXgrid + multiplier;
            }
            currentYgrid++;
        }
        if (roomNumber == 5) //ballroom
        {
            if (roomDirection == 1)
            {
                yC+= 20*multiplier;
            }
            else 
            {
                yC-= 20*3; //up 3
                currentYgrid = currentYgrid - 3;
                if (roomDirection == 3) //right
                {
                    xC+= 21; 
                    currentXgrid++;
                }
                else
                {
                    xC-= 21; 
                    currentXgrid--;
                }
                yC+= 20*multiplier;
                currentYgrid = currentYgrid - multiplier;
            }
        }
        repaint();
    }
    
    // @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(gameboard.getImage(), 0, 0, null);
        Rectangle bounds = getBounds(xC, yC);
        g.setColor(new Color(currentPlayer.getCharacter().getColor()));
        g.fillRect((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getHeight(), (int) bounds.getWidth());
    }

    private Rectangle getBounds(int x, int y) {
        //offset
        //x=x+30;
        //y=y+16;
        return new Rectangle(x + 30,y + 16,20,20);
    }

    private void requestBtns(int x, int y) throws IOException, ClassNotFoundException{
    	int[] coords = {0,0};
    	coords[0] = x;
    	coords[1] = y;
    	String xc = String.valueOf(coords[0]);
                String yc = String.valueOf(coords[1]);
                String s = (", ");
                String coordinates = xc.concat(s).concat(yc);
                clientFrame.addToLogConsole(coordinates); //adds player location to console

        client.send(new Message(ClueGameConstants.REQUEST_MOVEMENT_BUTTON_VALUES, coords));
        messageReceived = client.getMessage();
        btnValues =  (String) messageReceived.getData();
        cArray_movement_enter = btnValues.toCharArray();
        //for debugging purposes, output string to console log
        clientFrame.addToLogConsole(btnValues); 
    }

    private void enableOrdisableBtns(JButton movementButtons[], JButton enterButton[], char cArray_movement_enter[]){
    	//WEST = 0, EAST = 1, NORTH = 2, SOUTH = 3;
        boolean []moveOptions = {false,false,false,false};
        boolean []roomOptions = {false};
        int i=1;

        if (cArray_movement_enter[0] == '1')
        {
            roomOptions[0] = true;
        }
        else 
        {
            roomOptions[0] = false;
        }
        
        for (int j=0; j<moveOptions.length; j++)
        {
            if (cArray_movement_enter[i] == '1')
            {
                moveOptions[j] = true;
            }
            else
            {
                moveOptions[j] = false;
            }
            i++;
        }

    	movementButtons[WEST].setEnabled(moveOptions[WEST]);
    	movementButtons[EAST].setEnabled(moveOptions[EAST]);
    	movementButtons[NORTH].setEnabled(moveOptions[NORTH]);
        movementButtons[SOUTH].setEnabled(moveOptions[SOUTH]);
        enterButton[ENTER_ROOM].setEnabled(roomOptions[ENTER_ROOM]);    
    }

    public void requestBtnsCall(int currentXgrid, int currentYgrid)
    {
        try {
            requestBtns(currentXgrid, currentYgrid);
        } catch (ClassNotFoundException | IOException e1) {
            e1.printStackTrace();
        }
    }

     /* TODO ---- 
    private void updatePlayerLocations() throws IOException, ClassNotFoundException{
    	client.send(new Message(ClueGameConstants.REQUEST_PLAYER_MAP, null));
    	messageReceived = client.getMessage();
    } */

    private void initComponents()
    {
        this.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        this.setBackground(Color.PINK);
        this.setBounds(6, 37, 688, 535);

        movementButtons = new BasicArrowButton[4];
        movementButtons[NORTH] = new BasicArrowButton(BasicArrowButton.NORTH);
        movementButtons[NORTH].setBounds(613, 74, 26, 23);
        this.add(movementButtons[NORTH]);

        movementButtons[SOUTH] = new BasicArrowButton(BasicArrowButton.SOUTH);
        movementButtons[SOUTH].setBounds(613, 136, 26, 23);
        this.add(movementButtons[SOUTH]);

        movementButtons[EAST] = new BasicArrowButton(BasicArrowButton.EAST);
        movementButtons[EAST].setBounds(639, 106, 26, 23);
        this.add(movementButtons[EAST]);
        
        movementButtons[WEST] = new BasicArrowButton(BasicArrowButton.WEST);
        movementButtons[WEST].setBounds(587, 106, 26, 23);
        this.add(movementButtons[WEST]);

        enterButton = new JButton[1];
        enterButton[ENTER_ROOM] = new JButton("Enter Room");
        enterButton[ENTER_ROOM] .setForeground(new Color(128, 0, 128));
        enterButton[ENTER_ROOM] .setFont(new Font("SansSerif", Font.BOLD, 10));
        enterButton[ENTER_ROOM] .setBounds(579, 340, 99, 23);
        this.add(enterButton[ENTER_ROOM]);
        
        JButton btnExitRoom = new JButton("Exit Room");
        btnExitRoom.setForeground(new Color(128, 0, 128));
        btnExitRoom.setFont(new Font("SansSerif", Font.BOLD, 10));
        btnExitRoom.setBounds(579, 375, 99, 23);
        this.add(btnExitRoom);

        JButton btnSuggest = new JButton("Suggest");
        btnSuggest.setFont(new Font("SansSerif", Font.BOLD, 10));
        btnSuggest.setBounds(579, 410, 99, 23);
        this.add(btnSuggest);

        JButton btnAccuse = new JButton("Accuse");
        btnAccuse.setFont(new Font("SansSerif", Font.BOLD, 10));
        btnAccuse.setBounds(579, 445, 99, 23);
        this.add(btnAccuse);

        JButton btnShortcut = new JButton("Shortcut");
        btnShortcut.setForeground(Color.BLACK);
        btnShortcut.setFont(new Font("SansSerif", Font.BOLD, 10));
        btnShortcut.setBounds(579, 480, 99, 23);
        this.add(btnShortcut);

        JButton btnEndTurn = new JButton("End Turn");
        btnEndTurn.setForeground(Color.RED);
        btnEndTurn.setFont(new Font("SansSerif", Font.BOLD, 10));
        btnEndTurn.setBounds(579, 243, 99, 23);
        this.add(btnEndTurn);

        btnRollDice = new JButton("Roll Dice");
        btnRollDice.setForeground(new Color(0,128,0));
        btnRollDice.setFont(new Font("SansSerif", Font.BOLD, 10));
        btnRollDice.setBounds(579, 208, 99, 23);
        this.add(btnRollDice);
    }

} // end class