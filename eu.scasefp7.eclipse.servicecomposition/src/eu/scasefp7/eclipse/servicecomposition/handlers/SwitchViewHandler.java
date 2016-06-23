package eu.scasefp7.eclipse.servicecomposition.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import eu.scasefp7.eclipse.servicecomposition.views.ServiceCompositionView;

public class SwitchViewHandler extends AbstractHandler{

	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {


		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ServiceCompositionView.ID);
		} catch (PartInitException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		return null;
	}
}
