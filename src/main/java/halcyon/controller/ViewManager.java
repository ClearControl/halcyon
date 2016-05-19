package halcyon.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import halcyon.view.HalcyonPanel;
import halcyon.view.TreePanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;

import halcyon.model.collection.HalcyonNodeRepository;
import halcyon.model.collection.HalcyonNodeRepositoryListener;
import halcyon.model.collection.ObservableCollection;
import halcyon.model.collection.ObservableCollectionListener;
import halcyon.model.node.HalcyonOtherNode;
import halcyon.model.node.HalcyonNode;
import halcyon.model.node.HalcyonNodeInterface;
import halcyon.model.node.HalcyonSwingNode;
import halcyon.view.console.StdOutputCaptureConsole;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.SplitPane;

/**
 * ViewManager is a controller class to manage HalcyonNodes and GUI.
 */
public class ViewManager
{
	private final List<HalcyonPanel > mPages = new LinkedList<>();

	private final HashMap<HalcyonNodeInterface, Stage> mExternalNodeMap = new HashMap<>();

	/** a set of {@link HalcyonNode}s */
	private final HalcyonNodeRepository mNodes;

	private final DockPane mDockPane;

	private final TreePanel mTreePanel;

	private final StdOutputCaptureConsole mStdOutputCaptureConsole;

	private final Menu mViewMenu;

	/**
	 * Instantiates a new ViewManager.
	 * @param pDockPane the DockPane
	 * @param pTreePanel the TreePanel
	 * @param nodes the HalcyonNodes
	 * @param pConsoles the ConsolePanel collection
	 * @param pToolbars the ToobalPanel collection
	 * @param pViewMenu the ViewMenu
	 */
	public ViewManager(	DockPane pDockPane,
											TreePanel pTreePanel,
											HalcyonNodeRepository nodes,
											ObservableCollection<DockNode> pConsoles,
											ObservableCollection<DockNode> pToolbars,
											Menu pViewMenu)
	{
		this.mDockPane = pDockPane;
		this.mNodes = nodes;

		this.mDockPane.setPrefSize( 800, 600 );
		this.mViewMenu = pViewMenu;

		mTreePanel = pTreePanel;
		mTreePanel.setPrefSize(200, 300);
		mTreePanel.setClosable( false );
		mTreePanel.dock(this.mDockPane, DockPos.LEFT);

		mStdOutputCaptureConsole = new StdOutputCaptureConsole();
		mStdOutputCaptureConsole.setPrefSize(600, 200);
		mStdOutputCaptureConsole.setClosable( false );
		pConsoles.add(mStdOutputCaptureConsole);

		addViewMenuItem( "Console", mStdOutputCaptureConsole );

		dockNodes(pDockPane, DockPos.RIGHT, mTreePanel, pConsoles);
		dockNodes(pDockPane, DockPos.TOP, mTreePanel, pToolbars);

		SplitPane split = (SplitPane) pDockPane.getChildren().get(0);
		split.setDividerPositions(0.3);

		pConsoles.addListener(new ObservableCollectionListener<DockNode>()
		{
			@Override
			public void itemAdded(DockNode item)
			{
				addViewMenuItem( "Console", item );
			}

			@Override
			public void itemRemoved(DockNode item)
			{

			}
		});

		pToolbars.addListener(new ObservableCollectionListener<DockNode>()
		{
			@Override
			public void itemAdded(DockNode item)
			{
				addViewMenuItem( "Toolbar", item );
			}

			@Override
			public void itemRemoved(DockNode item)
			{

			}
		});

		nodes.addListener(new HalcyonNodeRepositoryListener()
		{
			@Override
			public void nodeAdded(HalcyonNodeInterface node)
			{
				// open(node);
			}

			@Override
			public void nodeRemoved(HalcyonNodeInterface node)
			{
				close(node);
			}
		});
	}

	private void dockNodes(	DockPane pDockPane,
													DockPos pPosition,
													DockNode pSibling,
													ObservableCollection<DockNode> pToolbars)
	{

		int i = 0;
		DockNode lFirstDockNode = null;
		for (DockNode lDockNode : pToolbars.getList())
		{
			if (i == 0)
			{
				lFirstDockNode = lDockNode;
				lDockNode.dock(pDockPane, pPosition, pSibling);
			}
			else
				lDockNode.dock(pDockPane, DockPos.CENTER, lFirstDockNode);
			i++;
		}
	}

	private void addViewMenuItem(	String pMenuGroupName,
														DockNode pControlWindowBase)
	{
		mViewMenu.getItems()
						.stream()
						.filter(c -> c.getText().equals(pMenuGroupName))
						.findFirst()
						.ifPresent(c -> {
							CheckMenuItem lMenuItem = new CheckMenuItem(pControlWindowBase.getTitle());

							lMenuItem.setSelected(!pControlWindowBase.isClosed());
							pControlWindowBase.closedProperty()
																.addListener(new ChangeListener<Boolean>()
																{
																	@Override
																	public void changed(ObservableValue<? extends Boolean> observable,
																											Boolean oldValue,
																											Boolean newValue)
																	{
																		lMenuItem.setSelected(!newValue);
																	}
																});

							lMenuItem.setOnAction(new EventHandler<ActionEvent>()
							{
								@Override
								public void handle(ActionEvent event)
								{
									if (pControlWindowBase.isClosed())
									{
										pControlWindowBase.dock( mDockPane,
																						pControlWindowBase.getLastDockPos(),
																						pControlWindowBase.getLastDockSibling());
									}
									else
									{
										lMenuItem.setSelected(true);
									}
								}
							});

							((Menu)c).getItems().add( lMenuItem );
						});

	}

	/**
	 * Gets the Halcyons.
	 * @return the nodes
	 */
	public HalcyonNodeRepository getNodes()
	{
		return mNodes;
	}

	/**
	 * Open the HalcyonNode.
	 * @param node the node
	 */
	public void open(HalcyonNodeInterface node)
	{
		if( mExternalNodeMap.containsKey( node ) ) {
			mExternalNodeMap.get( node ).requestFocus();
			return;
		}

		if (node instanceof HalcyonSwingNode)
		{
			HalcyonSwingNode lHalcyonSwingNode = (HalcyonSwingNode) node;
			if (!lHalcyonSwingNode.isDockable())
			{
				lHalcyonSwingNode.setVisible(true);
				return;
			}
		}
		else if (node instanceof HalcyonOtherNode)
		{
			HalcyonOtherNode lHalcyonExternalNode = (HalcyonOtherNode) node;
			lHalcyonExternalNode.setVisible(true);
			return;
		}

		// If users want to focus the opened dock, then focus and return
		for (final HalcyonPanel n : mPages )
		{
			if (n.getNode() == node && n.isDocked())
			{
				n.focus();
				return;
			}
		}

		DockNode deviceTabsDock = null;
		// Checking which dock window is docked
		for (final HalcyonPanel n : mPages )
		{
			if (n.isDocked())
			{
				deviceTabsDock = n;
				break;
			}
		}

		// Otherwise, we will create new HalcyonNode
		final HalcyonPanel page = new HalcyonPanel(node);

		if( deviceTabsDock != null )
		{
			page.dock( mDockPane, DockPos.CENTER, deviceTabsDock);
		}
		else
		{
			page.dock( mDockPane, DockPos.TOP, mStdOutputCaptureConsole);
		}

		mPages.add( page );
	}

	/**
	 * Hide the HalcyonNode.
	 * @param node the node
	 */
	public void hide(HalcyonNodeInterface node)
	{
		if (node instanceof HalcyonSwingNode)
		{
			HalcyonSwingNode lHalcyonSwingNode = (HalcyonSwingNode) node;
			if (!lHalcyonSwingNode.isDockable())
			{
				lHalcyonSwingNode.setVisible(false);
				return;
			}
		}
		else if (node instanceof HalcyonOtherNode)
		{
			HalcyonOtherNode lHalcyonExternalNode = (HalcyonOtherNode) node;
			lHalcyonExternalNode.setVisible(false);
			return;
		}

		for (final HalcyonPanel page : mPages.toArray(new HalcyonPanel[ mPages.size()]))
		{
			if (page.getNode() == node)
			{
				page.setVisible(false);
			}
		}
	}

	/**
	 * Close the HalcyonNode.
	 * @param node the node
	 */
	public void close(HalcyonNodeInterface node)
	{
		if (node instanceof HalcyonSwingNode)
		{
			HalcyonSwingNode lHalcyonSwingNode = (HalcyonSwingNode) node;
			if (!lHalcyonSwingNode.isDockable())
			{
				lHalcyonSwingNode.close();
				return;
			}
		}
		else if (node instanceof HalcyonOtherNode)
		{
			HalcyonOtherNode lHalcyonExternalNode = (HalcyonOtherNode) node;
			// Close() makes the application hangs. Use setVisible(false) instead.
			// lHalcyonExternalNode.close();
			lHalcyonExternalNode.setVisible(false);
			return;
		}
		else if ( mExternalNodeMap.containsKey( node ) )
		{
			mExternalNodeMap.get( node ).close();
			mExternalNodeMap.remove( node );
			return;
		}

		for (final HalcyonPanel page : mPages.toArray(new HalcyonPanel[ mPages.size()]))
		{
			if (page.getNode() == node)
			{
				page.close();
			}
		}
	}

	/**
	 * Is visible or not.
	 * @return the boolean
	 */
	public boolean isVisible()
	{
		return mDockPane.isVisible();
	}

	/**
	 * Make an independent window.
	 * @param node the node
	 */
	public void makeIndependentWindow( HalcyonNodeInterface node )
	{
		if( node instanceof HalcyonOtherNode) {
			open( node );
			return;
		}

		for (final HalcyonPanel page : mPages.toArray(new HalcyonPanel[ mPages.size()]))
		{
			if (page.getNode() == node)
			{
				page.close();
				mPages.remove( page );
			}
		}

		if( !mExternalNodeMap.containsKey( node ) )
		{
			final Scene scene = mDockPane.getScene();

			BorderPane lBorderPane = new BorderPane();
			final Node lPanel = node.getPanel();
			lBorderPane.setCenter( lPanel );
			Scene lScene = new Scene( lBorderPane );

			Stage lStage = new Stage();
			lStage.setTitle( node.getName() );
			lStage.setScene( lScene );
			lStage.setX( scene.getWindow().getX() );
			lStage.setY( scene.getWindow().getY() );
			lStage.show();

			mExternalNodeMap.put( node, lStage );

			lStage.setOnCloseRequest( new EventHandler< WindowEvent >()
			{
				@Override public void handle( WindowEvent event )
				{
					mExternalNodeMap.remove( node );
				}
			} );
		}
	}
}
