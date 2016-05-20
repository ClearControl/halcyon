package halcyon.demo;

import halcyon.HalcyonFrame;
import halcyon.model.node.HalcyonNode;
import halcyon.model.node.HalcyonNodeType;

import java.util.ArrayList;

import halcyon.view.TreePanel;
import javafx.application.Application;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.dockfx.DockNode;

public class DemoHalcyonMain extends Application
{

	@Override
	public void start(Stage pPrimaryStage) throws Exception
	{
		// TODO: support other type of devices
		final String lRootIconPath = DemoResourcesUtil.getString( "root.icon" );

		ArrayList<HalcyonNodeType> lNodeTypeList = new ArrayList<HalcyonNodeType>();
		for (HalcyonNodeType lHalcyonNodeType : DemoHalcyonNodeType.values())
			lNodeTypeList.add(lHalcyonNodeType);
		
		TreePanel lTreePanel = new TreePanel("Config",
									"Test Microscopy",
									getClass().getResourceAsStream(lRootIconPath),
									lNodeTypeList);

		final HalcyonFrame lHalcyonFrame = new HalcyonFrame(800,600);

		lHalcyonFrame.setTreePanel( lTreePanel );

		final HalcyonNode lLaser1 = HalcyonNode.wrap(	"Laser-1",
																									DemoHalcyonNodeType.ONE,
																									new VBox());

		final HalcyonNode lLaser2 = HalcyonNode.wrap(	"Laser-2",
																									DemoHalcyonNodeType.ONE,
																									new VBox());

		final HalcyonNode lCamera = HalcyonNode.wrap(	"Camera-1",
																									DemoHalcyonNodeType.TWO,
																									new VBox());

		final HalcyonNode lStage1 = HalcyonNode.wrap(	"Stage-1",
																									DemoHalcyonNodeType.THREE,
																									new VBox());

		lHalcyonFrame.addNode(lLaser1);
		lHalcyonFrame.addNode(lLaser2);
		lHalcyonFrame.addNode(lCamera);
		lHalcyonFrame.addNode(lStage1);

		// Custom DemoToolbar provided here
		DockNode lToolbar = new DemoToolbarPanel();
		lToolbar.setPrefSize( 300, 200 );
		lHalcyonFrame.addToolbar(lToolbar);
		lHalcyonFrame.addToolbar(new DemoSecondToolbarPanel());

		pPrimaryStage.setOnCloseRequest(event -> System.exit(0));

		lHalcyonFrame.start(pPrimaryStage);
	}

	public static void main(String[] args) throws Exception
	{
		launch(args);
	}
}
