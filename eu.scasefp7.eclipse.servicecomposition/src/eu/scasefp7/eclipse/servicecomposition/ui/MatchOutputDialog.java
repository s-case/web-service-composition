package eu.scasefp7.eclipse.servicecomposition.ui;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import eu.scasefp7.eclipse.servicecomposition.codeInterpreter.Value;
import eu.scasefp7.eclipse.servicecomposition.importer.Importer.Argument;
import eu.scasefp7.eclipse.servicecomposition.importer.JungXMIImporter.Connector;
import eu.scasefp7.eclipse.servicecomposition.transformer.JungXMItoOwlTransform.OwlService;
import eu.scasefp7.eclipse.servicecomposition.ui.TreeDialog.OperationNode;
import eu.scasefp7.eclipse.servicecomposition.ui.TreeDialog.ReplaceInformationNode;
import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView;



public class MatchOutputDialog extends Dialog {
	
	private Vector<Node> arrayNodes = new Vector<Node>();
	private String value;
	private Display disp;
	private String varName = "";
	private OwlService output;

	public MatchOutputDialog(Shell parentShell) {
		super(parentShell);
	}

	
	public void setArrayNodes(Vector<Node> arrayNodes) {
		this.arrayNodes = arrayNodes;
	}
	public String getValue() {
		return value;
	}
	public void setOutputService(OwlService service){
		this.output = service;
	}
	public void setName(String name) {
		this.varName = name;
	}
	public void setDisp(Display disp) {
		this.disp = disp;
	}
	public void setValue(String value) {
		this.value=value;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		container.setSize(200, 200);

		TreeViewer tree = new TreeViewer(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.getTree().setSize(200, 150);
		TreeViewerColumn columni = new TreeViewerColumn(tree, SWT.NONE);
		columni.getColumn().setWidth(200);
		columni.getColumn().setText("Columni");
		columni.getColumn().setResizable(true);
		TreeViewerColumn columnii = new TreeViewerColumn(tree, SWT.NONE);
		columnii.getColumn().setText("Columnii");
		columnii.getColumn().setWidth(200);
		columnii.getColumn().setResizable(true);
		tree.setContentProvider(new MyTreeContentProvider());

		columni.setLabelProvider(new MyLabelProvider());
		columnii.setLabelProvider(createColumnLabelProvider());
		tree.setInput(arrayNodes);
		
//		tree.addSelectionChangedListener(new ISelectionChangedListener() {
//			public void selectionChanged(SelectionChangedEvent event) {
//				if (event.getSelection() instanceof IStructuredSelection) {
//					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
//					if (selection.getFirstElement() != null) {
//						setValue(((Node) selection.getFirstElement()).getValue());
//						
//
//						
//					}
//
//				}
//			}
//		});
		
		
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					if (selection.getFirstElement() != null) {
						if (selection.getFirstElement() instanceof Node) {
							if (((Node) selection.getFirstElement()).getArgument().getOwlService().equals(output)) {
								Button okButton = getButton(IDialogConstants.OK_ID);
								okButton.setEnabled(true);
								setValue(((Node) selection.getFirstElement()).getValue());
							} else {
								Button okButton = getButton(IDialogConstants.OK_ID);
								okButton.setEnabled(false);
								tree.setSelection(StructuredSelection.EMPTY);
							}
						} 
					}

				}
			}
		});
		
		// the viewer field is an already configured TreeViewer
		Tree tree2 = (Tree) tree.getControl();

		Listener listener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				TreeItem treeItem = (TreeItem) event.item;
				final TreeColumn[] treeColumns = treeItem.getParent().getColumns();
				disp.asyncExec(new Runnable() {

					@Override
					public void run() {
						for (TreeColumn treeColumn : treeColumns)
							treeColumn.pack();
					}
				});
			}
		};

		tree2.addListener(SWT.Expand, listener);

		

		tree.expandAll();

		return container;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override
	public void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Choose output value to be matched with \"" + varName + "\" input variable");
		newShell.setMinimumSize(200, 200);
	}

	public void setDialogLocation() {
		Rectangle monitorArea = getShell().getDisplay().getPrimaryMonitor().getBounds();
		Rectangle shellArea = getShell().getBounds();
		int x = monitorArea.x + (monitorArea.width - shellArea.width) / 2;
		int y = monitorArea.y + (monitorArea.height - shellArea.height) / 2;
		getShell().setLocation(x, y);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}
	
	public class MyLabelProvider extends ColumnLabelProvider implements ILabelProvider {
		public String getText(Object element) {
			return ((Node) element).getName();
		}

		public Image getImage(Object arg0) {
			return null;
		}

		public void addListener(ILabelProviderListener arg0) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object arg0, String arg1) {
			return false;
		}

		public void removeListener(ILabelProviderListener arg0) {
		}
	}

	public class MyTreeContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object parentElement) {
			Vector<Node> subcats = ((Node) parentElement).getSubCategories();
			return subcats == null ? new Object[0] : subcats.toArray();
		}

		public Object getParent(Object element) {
			return ((Node) element).getParent();
		}

		public boolean hasChildren(Object element) {
			return ((Node) element).getSubCategories() != null;
		}

		public Object[] getElements(Object inputElement) {
			if (inputElement != null && inputElement instanceof Vector) {
				return ((Vector<Node>) inputElement).toArray();
			}
			return new Object[0];
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private static ColumnLabelProvider createColumnLabelProvider() {
		return new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return ((Node) element).getValue();
			}

		};
	}
	

}

