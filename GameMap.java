package spillprosjekt;

import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GameMap {
	private Room[][] rooms;
	private int numberOfX = 20;
	private int numberOfY = 18;
	private Group roomImages = new Group();
	private Group background;
	private Group mainGroup;
	private Group mainGroup1;
	private Group instructionGroup;
	private Group dagBokGroup;
	public final static int pixels = 32;
	private PlayerView playerV;
	private ErikFXHoved hoved;
	private Text lifeV;
	private Text hungerV;
	private Text foodV;
	private Text bandagesV;
	private Text shotsV;
	private Text inventoryV;
	private Button howToKnapp;
	private Boolean stromPa = false;
	
	
	public GameMap(){
		hoved = new ErikFXHoved();
		hoved.start();
		initInstructionScreen();
		initDagBokScreen();
		initGameMap(hoved.getBrettInt());
	}
	
	private void initGameMap(ArrayList<Integer> romListe){
		System.out.println(romListe);
		rooms = new Room[numberOfX][numberOfY];
		for (int x = 0; x < numberOfX; x++){
			for (int y = 0; y < numberOfY; y++){
				rooms[x][y] = new Room(romListe.get(0),x,y);
				romListe.remove(0);
			}
		}
		for (int x = 0; x < numberOfX; x++){
			for (int y = 0; y < numberOfY; y++){
				rooms[x][y].setTranslateX((x*pixels));
				rooms[x][y].setTranslateY((y*pixels)+24);
				rooms[x][y].setVisible(true);
				roomImages.getChildren().add(rooms[x][y]);	
			}
		}
		
		howToKnapp = new Button("Hva gj�r knappene??");
		howToKnapp.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent arg0) {
				oppdaterDagBokScreen();
				mainGroup1.setVisible(false);
				instructionGroup.setVisible(true);
			}
		});
		initBackground();
		playerV = new PlayerView((10*pixels)+9,10*pixels);
		Group player = new Group(playerV);
		mainGroup1 = new Group(initBackground(), roomImages,player, initStatView(), howToKnapp);
		mainGroup1.setVisible(false);
		dagBokGroup.setVisible(false);
		mainGroup = new Group(mainGroup1, instructionGroup, dagBokGroup);
	}
	
	private void initInstructionScreen(){
		Text instructionText = new Text("Her er det forklart hva knappene gj�r:");
		Text pilTastOpp = new Text("piltast opp: beveger deg opp.");
		Text pilTastNed = new Text("piltast ned: beveger deg ned.");
		Text pilTastVenstre = new Text("piltast venstre: beveger deg til venstre.");
		Text pilTastHoyre = new Text("piltast opp: beveger deg til h�yre.");
		Text I = new Text("i: �pner inventoryen.");
		Text S = new Text("s: s�ker i det rommet du er i, hvis du kan gj�re det.");
		Text R = new Text("r: leser det du har funnet s� langt av dagboken.");
		Text E = new Text("e: spiser mat, hvis du har det.");
		Text B = new Text("b: bandasjerer deg selv.");
		Text P = new Text("p: fors�ker � skru p� str�mmen i det rommet du er i.");
		Button tilbakeKnapp = new Button("Start spillet!");
		tilbakeKnapp.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				mainGroup1.setVisible(true);
				instructionGroup.setVisible(false);
				tilbakeKnapp.setText("Tilbake til spillet.");
			}
			
		});
		VBox boks = new VBox(tilbakeKnapp, instructionText, pilTastOpp, pilTastNed, pilTastVenstre, pilTastHoyre, I, S, R, E, B, P);
		BoksIterator boksIterator = new BoksIterator(boks);
		while(boksIterator.hasNext()){ 
			Object node = boksIterator.next();
			if(node instanceof Text){
				((Text) node).setFont(Font.font("Kai", 24));
			}
		}
		instructionGroup = new Group(boks);
	}
	
	private void initDagBokScreen(){
		ArrayList<String> sider = hoved.getHelBok();
		VBox boks = new VBox();
		for(String side : sider){
			boks.getChildren().add(new Text(side));
		}
		dagBokGroup = new Group(boks);
	}
	
	private void oppdaterDagBokScreen(){
		for(int i = 0; i < Dagbok.getAntallSiderTotal(); i++){
			ArrayList<String> liste = hoved.getbok();
			Node s = ((VBox) dagBokGroup.getChildren().get(0)).getChildren().get(i);
			if(liste.get(i).equals("tom side")){
				System.out.println(((Text) s).getText());
				s.setVisible(false);
				System.out.println("falsk");
			}
			else{
				s.setVisible(true);
				System.out.println("sann");
			}
		}
	}
	
	private Group initBackground(){
		Image background = new Image("file:images/bakgrunn.png");
		ImageView view = new ImageView(background);
		view.setFitWidth(640);
		this.background = new Group(view);
		return this.background;
		
	}
	
	public Group getMainGroup(){
		return mainGroup;
	}
	
	
	public void handle(KeyEvent e) {
		switch(e.getCode()){
		case UP:
			playerV.movePlayer('w', rooms);
			hoved.opp();
			break;
		case DOWN:
			playerV.movePlayer('s', rooms);
			hoved.ned();
			break;
		case LEFT:
			playerV.movePlayer('a', rooms);
			hoved.venstre();
			break;
		case RIGHT:
			playerV.movePlayer('d', rooms);
			hoved.hoyre();
			break;
		case I:
			hoved.inventory();
			break;
		case S:
			hoved.sok();
			FXSok();
			break;
		case R:
			hoved.les();
			if(dagBokGroup.isVisible()){
				dagBokGroup.setVisible(false);
				mainGroup1.setVisible(true);
			}
			else{
				dagBokGroup.setVisible(true);
				mainGroup1.setVisible(false);
			}
			break;
		case E:
			hoved.spis();
			break;
		case B:
			hoved.bandasje();
			break;
		case P:
			FXSkruPa();
			break;
		default:
			break;
		}
		oppdaterTilstand();
	}
	
	private void oppdaterTilstand(){
		lifeV.setText(Integer.toString(hoved.getLiv()));
		hungerV.setText(Integer.toString(hoved.getSult()));
		foodV.setText(Integer.toString(hoved.antallMat()));
		bandagesV.setText(Integer.toString(hoved.antallBandasje()));
		shotsV.setText(Integer.toString(hoved.antallSkudd()));
		if(sjekkStrom()){
			lifeV.setText("Eqrwerwer");
			this.stromPa = true;
		}
	}
	
	private boolean sjekkStrom() {
		for(Room[] rad : rooms){
			for(Room rom : rad){
				if(rom.getType() == 4){					
					return false;
				}
			}			
		} 
		return true;
	}

	private Group initStatView(){
		Text life = new Text(50,50,"Life:");
		Text hunger = new Text(50,50,"Hunger:");
		Text food = new Text(50,50,"Food:");
		Text bandages = new Text(50,50,"Bandages:");
		Text shots = new Text(50,50,"Shots:");
		Text inventory = new Text(50,50,"Inventory");
		lifeV = new Text(50,50,"100");
		hungerV = new Text(50,50,"100");
		foodV = new Text(50,50,"0");
		bandagesV = new Text(50,50,"0");
		shotsV = new Text(50,50,"0");
		VBox topLeft = new VBox(life,hunger,food,bandages,shots);
		VBox topRight = new VBox(this.lifeV, this.hungerV,this.foodV, this.bandagesV, this.shotsV);
		for (int x = 0; x < 5; x++){
			Text text = (Text) topLeft.getChildren().get(x);
			Text text2 = (Text) topRight.getChildren().get(x);
			text.setFont(Font.font("Kai",18));
			text2.setFont(Font.font("Kai",18));
		
			
		}
		topLeft.setTranslateX(660);
		topLeft.setTranslateY(24);
		topLeft.setSpacing(20);
		topRight.setTranslateX(765);
		topRight.setTranslateY(24);
		topRight.setSpacing(20);
		Group statView = new Group(topLeft,topRight);
		return statView;
		
		
	}

	private void FXSok() {
		int x = playerV.getRoomXPos();
		int y = playerV.getRoomYPos();
		rooms[x][y].sok();
	}
	
	private void FXSkruPa(){
		int x = playerV.getRoomXPos();
		int y = playerV.getRoomYPos();
		rooms[x][y].skruPa();
	}
}
