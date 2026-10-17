package android.print;

import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.IOException;

public class PdfPrint {

    public interface Callback {
        void onSuccess(File file);
        void onFailure(String error);
    }

    private final PrintAttributes printAttributes;

    public PdfPrint(PrintAttributes printAttributes) {
        this.printAttributes = printAttributes;
    }

    public void print(final PrintDocumentAdapter adapter, final File path, final String fileName, final Callback callback) {
        if (!path.exists()) {
            path.mkdirs();
        }
        final File file = new File(path, fileName);
        try {
            final ParcelFileDescriptor pfd = ParcelFileDescriptor.open(
                    file,
                    ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_TRUNCATE
            );

            adapter.onLayout(null, printAttributes, null, new PrintDocumentAdapter.LayoutResultCallback() {
                @Override
                public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                    adapter.onWrite(new PageRange[]{PageRange.ALL_PAGES}, pfd, new CancellationSignal(), new PrintDocumentAdapter.WriteResultCallback() {
                        @Override
                        public void onWriteFinished(PageRange[] pages) {
                            try {
                                pfd.close();
                            } catch (IOException ignored) {}
                            if (callback != null) {
                                callback.onSuccess(file);
                            }
                        }

                        @Override
                        public void onWriteFailed(CharSequence error) {
                            try {
                                pfd.close();
                            } catch (IOException ignored) {}
                            if (callback != null) {
                                callback.onFailure(error != null ? error.toString() : "Write failed");
                            }
                        }

                        @Override
                        public void onWriteCancelled() {
                            try {
                                pfd.close();
                            } catch (IOException ignored) {}
                            if (callback != null) {
                                callback.onFailure("Write cancelled");
                            }
                        }
                    });
                }

                @Override
                public void onLayoutFailed(CharSequence error) {
                    try {
                        pfd.close();
                    } catch (IOException ignored) {}
                    if (callback != null) {
                        callback.onFailure(error != null ? error.toString() : "Layout failed");
                    }
                }

                @Override
                public void onLayoutCancelled() {
                    try {
                        pfd.close();
                    } catch (IOException ignored) {}
                    if (callback != null) {
                        callback.onFailure("Layout cancelled");
                    }
                }
            }, null);

        } catch (Exception e) {
            if (callback != null) {
                callback.onFailure(e.getMessage());
            }
        }
    }
}
