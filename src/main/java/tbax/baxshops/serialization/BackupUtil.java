/*
 * Copyright (C) 2013-2019 Timothy Baxendale
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package tbax.baxshops.serialization;

import tbax.baxshops.Format;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

class BackupUtil
{
    public static boolean backup(SavedState savedState)
    {
        File stateLocation = savedState.getFile();
        if (!stateLocation.exists()) {
            savedState.log.warning("Aborting backup: shops.yml not found");
            return false;
        }

        File backupFolder = savedState.getBackupFile();
        if (!backupFolder.exists() && !backupFolder.mkdirs()) {
            savedState.log.severe("Unable to create backups folder!");
            return false;
        }

        try {
            String backupName = Format.FILE_DATE_FORMAT.format(new Date()) + ".yml";
            File backup = new File(backupFolder, backupName);
            try (InputStream in = new FileInputStream(stateLocation)) {
                try (OutputStream out = new FileOutputStream(backup)) {
                    byte[] buf = new byte[1024];
                    int i;
                    while ((i = in.read(buf)) > 0) {
                        out.write(buf, 0, i);
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            savedState.log.severe("Backup failed!");
            return false;
        }
        return true;
    }

    public static void deleteOldestBackup(SavedState savedState)
    {
        File backupFolder = savedState.getBackupFile();
        List<File> backups = getBackupFiles(backupFolder);
        int nBaks = savedState.getConfig().getBackups();

        if (backups == null || nBaks <= 0 || backups.size() < nBaks) {
            return;
        }

        while (backups.size() >= nBaks) {
            File delete = new File(backupFolder, backups.remove(backups.size() - 1) .getName() + ".yml");
            if (!delete.delete()) {
                savedState.log.warning(String.format("Unable to delete old backup %s", delete.getName()));
            }
        }
    }

    public static List<File> getBackupFiles(File backupFolder)
    {
        return Arrays.stream(backupFolder.listFiles((f, name) -> name.endsWith(".yml")))
                .map(f -> f.getName().substring(0, f.getName().lastIndexOf('.')))
                .filter(n -> Format.parseFileDate(n) != null)
                .map(n -> new File(backupFolder, n + ".yml"))
                .sorted(Comparator.comparing(f -> Format.parseFileDate(f.getName())))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    public static boolean deleteLatestBackup(SavedState savedState)
    {
        List<File> backups = getBackupFiles(savedState.getBackupFile());
        if (backups.isEmpty())
            return false;
        return backups.get(0).delete();
    }

    public static boolean hasStateChanged(SavedState savedState)
    {
        List<File> backups = getBackupFiles(savedState.getBackupFile());
        if (backups.isEmpty())
            return true;

        File latest = backups.get(0);
        File state = savedState.getFile();

        if (latest.length() != state.length())
            return true;

        try {
            try (InputStream stream1 = new FileInputStream(latest);
                 InputStream stream2 = new FileInputStream(state)) {
                int b1;
                while((b1 = stream1.read()) != -1) {
                    if (b1 != stream2.read()) {
                        return true;
                    }
                }
            }
            return false;
        }
        catch (IOException e) {
            return true;
        }
    }
}
