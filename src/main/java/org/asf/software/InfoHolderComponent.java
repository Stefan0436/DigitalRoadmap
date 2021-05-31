package org.asf.software;

import java.awt.Component;
import java.awt.Dimension;
import java.util.function.Supplier;

public class InfoHolderComponent extends Component {

	private static final long serialVersionUID = 1L;

	private Supplier<String> dataSupplier;

	public InfoHolderComponent(Supplier<String> dataSupplier) {
		this.dataSupplier = dataSupplier;
		setPreferredSize(new Dimension(0, 0));
		setVisible(false);
	}

	public String getData() {
		return dataSupplier.get();
	}

}
