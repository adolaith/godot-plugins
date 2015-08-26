package application;

import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Main extends Application {
	int width, height, tileW, tileH;
	ArrayList<ImageView> gendTiles;
	Path savedDirs;
	
	ToggleGroup selectedDir;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		width = 830;
		height = 400;
		
		try {
			savedDirs = Paths.get(System.getProperty("user.home") + File.separator + "tileGen");
			
			if(Files.notExists(savedDirs)){
				Files.createDirectory(savedDirs);
			}
			
			savedDirs = Paths.get(savedDirs.toString() + File.separator + "bookmarks");
			
			primaryStage.setTitle("TileGen Tool");
			primaryStage.resizableProperty().set(false);
			
			//root pane
			VBox root = new VBox();
			Scene scene = new Scene(root, width, height);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			
			MenuBar menu = createMenu(primaryStage, scene);
			
			((VBox) scene.getRoot()).getChildren().addAll(menu);
			
			//body pane
			GridPane grid = new GridPane();
			grid.setAlignment(Pos.TOP_LEFT);
			grid.setHgap(10);
//			grid.setGridLinesVisible(true);
			grid.setPadding(new Insets(25, 25, 25, 25));
			root.getChildren().add(grid);
			
			//create tile, edge and fill mask panes
			GridPane lists = createImgLists();
			grid.add(lists, 0, 0);
			
			//create add tile, edge, fill mask buttons
			GridPane addButtons = createButtons(primaryStage, scene);
			grid.add( addButtons, 0, 1);
			
			//generated images pane
			ScrollPane scroll = new ScrollPane();
			scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
			scroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
			GridPane.setVgrow(scroll, Priority.ALWAYS);
			GridPane.setHgrow(scroll, Priority.ALWAYS);
			
			grid.add(scroll, 1, 0);
			
			FlowPane imgPane = new FlowPane();
			imgPane.setId("generated");
			imgPane.setHgap(5);
			imgPane.setVgap(5);
			scroll.setContent(imgPane);
			
			GridPane genPane = new GridPane();
			//generate tiles buttons
			Button btn = new Button("Generate");
			GridPane.setMargin(btn, new Insets(10, 0, 10, 0));
			btn.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					Generator.generateTiles(scene);					
				}
			});
			
			genPane.add(btn, 0, 0);
			
			btn = new Button("Clear");
			GridPane.setMargin(btn, new Insets(10, 0, 10, 0));
			btn.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					((FlowPane)scene.lookup("#generated")).getChildren().clear();
				}
			});
			
			genPane.add(btn, 1, 0);
			
			grid.add(genPane, 1, 1);
			
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private GridPane createButtons(Stage stage, Scene scene){
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_LEFT);
		grid.setHgap(10);
		
		Button btn = new Button("Add Tile");
		GridPane.setMargin(btn, new Insets(10, 0, 10, 0));
		btn.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				FileChooser fileWin = new FileChooser();
				ExtensionFilter filter = new ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg");
				fileWin.getExtensionFilters().add(filter);
				
				if(selectedDir.getSelectedToggle() != null){
					fileWin.setInitialDirectory(new File(selectedDir.getSelectedToggle().getUserData().toString()));
				}
				
				File f = fileWin.showOpenDialog(stage);
				if(f != null){
					ImageView img = getImage(new Image(f.toURI().toString()));
					if((img.getImage().getWidth() == tileW && img.getImage().getHeight() == tileH)
							|| tileW == 0){
						
						img.setOnMouseClicked(new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent event) {
								((VBox)scene.lookup("#tiles")).getChildren().remove(img);
								
								if(((VBox)scene.lookup("#tiles")).getChildren().size() == 0){
									tileW = 0;
									tileH = 0;
								}
							}							
						});
						
						((VBox)scene.lookup("#tiles")).getChildren().add(img);
						
						if(((VBox)scene.lookup("#tiles")).getChildren().size() == 1){
							tileW = (int) img.getImage().getWidth();
							tileH = (int) img.getImage().getHeight();
						}
					}
				}
			}
		});
		
		grid.add(btn, 0, 0);
		
		btn = new Button("Add edge mask");
		GridPane.setMargin(btn, new Insets(10, 0, 10, 0));
		btn.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				FileChooser fileWin = new FileChooser();
				ExtensionFilter filter = new ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg");
				fileWin.getExtensionFilters().add(filter);
				
				if(selectedDir.getSelectedToggle() != null){
					fileWin.setInitialDirectory(new File(selectedDir.getSelectedToggle().getUserData().toString()));
				}
				
				File f = fileWin.showOpenDialog(stage);
				if(f != null){
					ImageView img = getImage(new Image(f.toURI().toString()));
					img.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							((VBox)scene.lookup("#edges")).getChildren().remove(img);								
						}							
					});
					((VBox)scene.lookup("#edges")).getChildren().add(img);						
				}
			}
		});
		
		grid.add(btn, 1, 0);
		
		return grid;
	}
	
	private GridPane createImgLists(){
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_LEFT);
		grid.setHgap(30);
		
		VBox imgPane = createImgScroller(grid, 0, 0, 1, 6, width, height);
		imgPane.setId("tiles");
		
		VBox edgeMaskPane = createImgScroller(grid, 1, 3, 1, 1, width, height / 3);
		edgeMaskPane.setId("edges");
		
		return grid;
	}
	
	private MenuBar createMenu(Stage stage, Scene scene){
		MenuBar menu = new MenuBar();
		
		Menu menuFile = new Menu("File");
		
		MenuItem saveItem = new MenuItem("Save");
		saveItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
            	DirectoryChooser dirDialog = new DirectoryChooser();
            	dirDialog.setTitle("Save all tiles to...");
				
				File f = dirDialog.showDialog(stage);
				if(f != null){
					int count = 0;
					for(Node n: ((FlowPane)scene.lookup("#generated")).getChildren()){
						File pic = new File(f, "tile_"+ count +".png");
						
						RenderedImage renderedImage = SwingFXUtils.fromFXImage(((ImageView) n).getImage(), null);
						
						try {
							ImageIO.write(renderedImage, "png",	pic);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						count++;
					}
				}

            }
        });
		
		MenuItem quitItem = new MenuItem("Quit");
		quitItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                System.exit(0);
            }
        });
		
		menuFile.getItems().addAll(saveItem, quitItem);
		
		Menu menuBookmarks = new Menu("Bookmarks");
		
		Menu listItem = new Menu("Saved dirs");
		selectedDir = new ToggleGroup();
		
		if(Files.exists(savedDirs)){
			try{
				BufferedReader reader = Files.newBufferedReader(savedDirs);
				
				String dir = null;
				
				while((dir = reader.readLine()) != null){
					newRadioItem(dir, listItem);
				}
				reader.close();
			}catch(Exception e){
				System.out.println("error: " + e);
			}
		}
		
		MenuItem addItem = new MenuItem("Add dir");
		addItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
            	try {
					DirectoryChooser dirDialog = new DirectoryChooser();
					
					File f = dirDialog.showDialog(stage);
					if(f != null){
						PrintWriter output = new PrintWriter(new FileWriter(savedDirs.toFile(),true));
						output.println(f.toString());
						newRadioItem(f.toString(), listItem);
						output.close();
					}	            							
					
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });
					
		menuBookmarks.getItems().addAll(addItem, listItem);
		
		menu.getMenus().addAll(menuFile, menuBookmarks);
		
		return menu;
	}
	
	private VBox createImgScroller(GridPane root, int startCol, int startRow, int colSpan, int rowSpan, int width, int height){
		ScrollPane scroll = new ScrollPane();
		scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		scroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scroll.setPrefWidth(width / 5.5);
		scroll.setPrefHeight(height - 10);
		root.add(scroll, startCol, startRow, colSpan, rowSpan);
		
		VBox imgPane = new VBox();
		imgPane.setPrefWidth(width / 6);
		imgPane.setPrefHeight(height - 10);
		imgPane.setSpacing(10);
		scroll.setContent(imgPane);
		
		Text imgLabel = new Text("(Click an image to remove it)");
		imgLabel.setFont(Font.font(10));	
		root.add(imgLabel, startCol, startRow + rowSpan);
		
		return imgPane;
	}
	
	private ImageView getImage(Image img){
		ImageView view = new ImageView();
		view.setImage(img);
		view.setCursor(Cursor.HAND);
		view.setFitWidth(64 * 2);
		view.setFitHeight(40 * 2);
		view.setCache(true);
		view.setSmooth(true);
		
		return view;
	}
	
	private void newRadioItem(String s, Menu list){
		RadioMenuItem item = new RadioMenuItem(s);
		item.setUserData(s);
		item.setToggleGroup(selectedDir);
		list.getItems().add(item);
	}
		
}
