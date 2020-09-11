import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class FileChooser {
    private File[] files;
    public FileChooser() {
        JFileChooser dialog = new JFileChooser();
        dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        dialog.setApproveButtonText("Выбрать");//выбрать название для кнопки согласия
        dialog.setDialogTitle("Выберите файл для загрузки");// выбрать название
        dialog.setDialogType(JFileChooser.OPEN_DIALOG);// выбрать тип диалога Open или Save
        dialog.setMultiSelectionEnabled(true); // Разрегить выбор нескольки файлов
        dialog.setFileFilter(new FileNameExtensionFilter(".xml","xml")); // Разрешаем выбор .xml
        dialog.setAcceptAllFileFilterUsed(false); // Запрет на выбор файла любого другого типа
        int returnVal = dialog.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            files = dialog.getSelectedFiles();
        }
    }
    public File[] getFiles() { return files; }
}
