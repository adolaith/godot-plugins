package application;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class Generator {
	final static int TOP_LEFT = 0;
	final static int TOP_RIGHT = 1;
	final static int BOTTOM_RIGHT = 2;
	final static int BOTTOM_LEFT = 3;
	final static int FULL_TILE = 4;
	final static int LEFT_RIGHT = 5;
	final static int TOP_BOTTOM = 6;
	final static int TOP_CORNER = 7;
	final static int RIGHT_CORNER = 8;
	final static int LEFT_CORNER = 9;
	final static int BOTTOM_CORNER = 10;
	
	public static void generateTiles(Scene scene, boolean[] maskingOptions){
		ObservableList<Node> tiles = ((VBox)scene.lookup("#tiles")).getChildren();
		ObservableList<Node> edges = ((VBox)scene.lookup("#edges")).getChildren();
		if(edges.size() == 0 || tiles.size() == 0) return;
		
		for(int t = 0; t < tiles.size(); t++){
			Image currentTile = ((ImageView)tiles.get(t)).getImage();
			
			for(int second = 0; second < tiles.size(); second++){
				if(second == t) continue;
				
				PixelReader secondTile = ((ImageView)tiles.get(second)).getImage().getPixelReader();
				
				for(int i = 0; i < edges.size(); i++){
					ImageView mask = (ImageView)edges.get(i);
					
					WritableImage[] rotatedMask = mirrorMask(mask);
					
					for(int x = 0; x <= BOTTOM_CORNER; x++){
						if(x < FULL_TILE && !maskingOptions[0]) continue;
						if((x == LEFT_RIGHT || x == TOP_BOTTOM) && !maskingOptions[1]) continue;
						if((x >= TOP_CORNER && x <= BOTTOM_CORNER) && !maskingOptions[2]) continue;
						if(x == FULL_TILE && !maskingOptions[3]) continue;
						
						WritableImage newImg = new WritableImage(currentTile.getPixelReader(),
								(int) currentTile.getWidth(), (int) currentTile.getHeight());
						PixelWriter writer = newImg.getPixelWriter();
						
						switch(x){
						case TOP_LEFT:
							applyMask(rotatedMask[x].getPixelReader(), secondTile, writer,
									(int) rotatedMask[x].getWidth(), (int) rotatedMask[x].getHeight(), 0, 0);
							break;
						case TOP_RIGHT:
							applyMask(rotatedMask[x].getPixelReader(), secondTile, writer,
									(int) rotatedMask[x].getWidth(), (int) rotatedMask[x].getHeight(), 
									(int) (newImg.getWidth() - rotatedMask[x].getWidth()), 0);
							break;
						case BOTTOM_RIGHT:
							applyMask(rotatedMask[x].getPixelReader(), secondTile, writer,
									(int) rotatedMask[x].getWidth(), (int) rotatedMask[x].getHeight(), 
									(int) (newImg.getWidth() - rotatedMask[x].getWidth()), (int) (newImg.getHeight() - rotatedMask[x].getHeight()));
							break;
						case BOTTOM_LEFT:
							applyMask(mask.getImage().getPixelReader(), secondTile, writer,
									(int) mask.getImage().getWidth(), (int) mask.getImage().getHeight(), 
									0, (int) (newImg.getHeight() - mask.getImage().getHeight()));
							break;
						case FULL_TILE:
							applyMask(rotatedMask[TOP_LEFT].getPixelReader(), secondTile, writer,
									(int) rotatedMask[TOP_LEFT].getWidth(), (int) rotatedMask[TOP_LEFT].getHeight(), 0, 0);
							
							applyMask(rotatedMask[TOP_RIGHT].getPixelReader(), secondTile, writer,
									(int) rotatedMask[TOP_RIGHT].getWidth(), (int) rotatedMask[TOP_RIGHT].getHeight(), 
									(int) (newImg.getWidth() - rotatedMask[TOP_RIGHT].getWidth()), 0);
							
							applyMask(rotatedMask[BOTTOM_RIGHT].getPixelReader(), secondTile, writer,
									(int) rotatedMask[BOTTOM_RIGHT].getWidth(), (int) rotatedMask[BOTTOM_RIGHT].getHeight(), 
									(int) (newImg.getWidth() - rotatedMask[BOTTOM_RIGHT].getWidth()),
									(int) (newImg.getHeight() - rotatedMask[BOTTOM_RIGHT].getHeight()));
							
							applyMask(mask.getImage().getPixelReader(), secondTile, writer,
									(int) mask.getImage().getWidth(), (int) mask.getImage().getHeight(), 
									0, (int) (newImg.getHeight() - mask.getImage().getHeight()));
							break;
						case LEFT_RIGHT:
							applyMask(mask.getImage().getPixelReader(), secondTile, writer,
									(int) mask.getImage().getWidth(), (int) mask.getImage().getHeight(), 
									0, (int) (newImg.getHeight() - mask.getImage().getHeight()));
							applyMask(rotatedMask[TOP_RIGHT].getPixelReader(), secondTile, writer,
									(int) rotatedMask[TOP_RIGHT].getWidth(), (int) rotatedMask[TOP_RIGHT].getHeight(), 
									(int) (newImg.getWidth() - rotatedMask[TOP_RIGHT].getWidth()), 0);
							break;
						case TOP_BOTTOM:
							applyMask(rotatedMask[TOP_LEFT].getPixelReader(), secondTile, writer,
									(int) rotatedMask[TOP_LEFT].getWidth(), (int) rotatedMask[TOP_LEFT].getHeight(), 0, 0);
							applyMask(rotatedMask[BOTTOM_RIGHT].getPixelReader(), secondTile, writer,
									(int) rotatedMask[BOTTOM_RIGHT].getWidth(), (int) rotatedMask[BOTTOM_RIGHT].getHeight(), 
									(int) (newImg.getWidth() - rotatedMask[BOTTOM_RIGHT].getWidth()),
									(int) (newImg.getHeight() - rotatedMask[BOTTOM_RIGHT].getHeight()));
							break;
						case TOP_CORNER:
							applyMask(rotatedMask[TOP_LEFT].getPixelReader(), secondTile, writer,
									(int) rotatedMask[TOP_LEFT].getWidth(), (int) rotatedMask[TOP_LEFT].getHeight(), 0, 0);
							applyMask(rotatedMask[TOP_RIGHT].getPixelReader(), secondTile, writer,
									(int) rotatedMask[TOP_RIGHT].getWidth(), (int) rotatedMask[TOP_RIGHT].getHeight(), 
									(int) (newImg.getWidth() - rotatedMask[TOP_RIGHT].getWidth()), 0);
							break;
						case RIGHT_CORNER:
							applyMask(rotatedMask[TOP_RIGHT].getPixelReader(), secondTile, writer,
									(int) rotatedMask[TOP_RIGHT].getWidth(), (int) rotatedMask[TOP_RIGHT].getHeight(), 
									(int) (newImg.getWidth() - rotatedMask[TOP_RIGHT].getWidth()), 0);
							applyMask(rotatedMask[BOTTOM_RIGHT].getPixelReader(), secondTile, writer,
									(int) rotatedMask[BOTTOM_RIGHT].getWidth(), (int) rotatedMask[BOTTOM_RIGHT].getHeight(), 
									(int) (newImg.getWidth() - rotatedMask[BOTTOM_RIGHT].getWidth()),
									(int) (newImg.getHeight() - rotatedMask[BOTTOM_RIGHT].getHeight()));
							break;
						case LEFT_CORNER:
							applyMask(rotatedMask[TOP_LEFT].getPixelReader(), secondTile, writer,
									(int) rotatedMask[TOP_LEFT].getWidth(), (int) rotatedMask[TOP_LEFT].getHeight(), 0, 0);
							applyMask(mask.getImage().getPixelReader(), secondTile, writer,
									(int) mask.getImage().getWidth(), (int) mask.getImage().getHeight(), 
									0, (int) (newImg.getHeight() - mask.getImage().getHeight()));
							break;
						case BOTTOM_CORNER:
							applyMask(mask.getImage().getPixelReader(), secondTile, writer,
									(int) mask.getImage().getWidth(), (int) mask.getImage().getHeight(), 
									0, (int) (newImg.getHeight() - mask.getImage().getHeight()));
							applyMask(rotatedMask[BOTTOM_RIGHT].getPixelReader(), secondTile, writer,
									(int) rotatedMask[BOTTOM_RIGHT].getWidth(), (int) rotatedMask[BOTTOM_RIGHT].getHeight(), 
									(int) (newImg.getWidth() - rotatedMask[BOTTOM_RIGHT].getWidth()),
									(int) (newImg.getHeight() - rotatedMask[BOTTOM_RIGHT].getHeight()));
							break;
						}
						
						ImageView view = new ImageView();
						view.setImage(newImg);
						view.setCursor(Cursor.HAND);
						view.setFitWidth(64 * 2);
						view.setFitHeight(40 * 2);
						view.setCache(true);
//						view.setSmooth(true);
						
						view.setOnMouseClicked(new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent event) {
								((FlowPane)scene.lookup("#generated")).getChildren().remove(view);
							}							
						});
						
						((FlowPane)scene.lookup("#generated")).getChildren().add(view);
					}		
				}
				
			}
		}
	}
	
	private static void applyMask(PixelReader mask, PixelReader secondTile, PixelWriter writer, int endX, int endY, int offsetX, int offsetY){
		for(int x = 0; x < endX; x++){
			for(int y = 0; y < endY; y++){
				Color from = mask.getColor(x, y);
				if(from.isOpaque()){
					from = secondTile.getColor(offsetX + x, offsetY + y);
					writer.setColor(offsetX + x, offsetY + y, from);
				}								
			}
		}
	}
	
	private static WritableImage[] mirrorMask(ImageView mask){
		WritableImage[] mirroredMask = new WritableImage[3];
		int maskWidth = (int) mask.getImage().getWidth();
		int maskHeight = (int) mask.getImage().getHeight();
		
		PixelReader reader = mask.getImage().getPixelReader();
		
		WritableImage mirror = new WritableImage( maskWidth, maskHeight);
		PixelWriter writer = mirror.getPixelWriter();
		
		for(int x = 0; x < maskWidth; x++){
			for(int y = 0; y < maskHeight; y++){
				Color from = reader.getColor(x, y);
				writer.setColor(x, (int) ((maskHeight - 1) - y), from);
			}
		}
		
		mirroredMask[TOP_LEFT] = mirror;
		
		mirror = new WritableImage( maskWidth, maskHeight);
		writer = mirror.getPixelWriter();
		
		for(int x = 0; x < maskWidth; x++){
			for(int y = 0; y < maskHeight; y++){
				Color from = reader.getColor(x, y);
				writer.setColor( (int) ((maskWidth - 1) - x), (int) ((maskHeight - 1) - y), from);
			}
		}
		
		mirroredMask[TOP_RIGHT] = mirror;
		
		mirror = new WritableImage( maskWidth, maskHeight);
		writer = mirror.getPixelWriter();
		
		for(int x = 0; x < maskWidth; x++){
			for(int y = 0; y < maskHeight; y++){
				Color from = reader.getColor(x, y);
				writer.setColor((int) ((maskWidth - 1) - x), y, from);
			}
		}
		
		mirroredMask[BOTTOM_RIGHT] = mirror;
		
		return mirroredMask;
	}
	
	
}
