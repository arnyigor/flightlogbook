package com.arny.flightlogbook.additional;

class Old {
/*
    public boolean saveExcelFile(Context context, String fileName) {
        Row row;
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e(TAG, "Storage not available or read only");
            return false;
        }
        boolean success = false;

        //New Workbook
        Workbook wb = new HSSFWorkbook();

        Cell c = null;
        //Cell style for header row
        CellStyle cs = wb.createCellStyle();
        cs.setFillForegroundColor(HSSFColor.LIME.index);
        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        //New Sheet
        Sheet sheet_main = null;
        sheet_main = wb.createSheet(LOG_SHEET_MAIN);
        //create base row
        row = sheet_main.createRow(0);
        c = row.createCell(0);
        c.setCellValue(getString(R.string.str_date));
        c = row.createCell(1);
        c.setCellValue(getString(R.string.str_itemlogtime));
        c = row.createCell(2);
        c.setCellValue(getString(R.string.str_type_null));
        c = row.createCell(3);
        c.setCellValue(getString(R.string.str_regnum));
        c = row.createCell(4);
        c.setCellValue(getString(R.string.str_day_night));
        c = row.createCell(5);
        c.setCellValue(getString(R.string.str_vfr_ifr));
        c = row.createCell(6);
        c.setCellValue(getString(R.string.str_flight_type));
        c = row.createCell(7);
        c.setCellValue(getString(R.string.str_desc));

        cursorExport = db.getAllItems();
        if (cursorExport.moveToFirst()) {
            int idColROW_ID = cursorExport.getColumnIndex(DatabaseHandler.ROW_ID);
            int idColROW_DATE = cursorExport.getColumnIndex(DatabaseHandler.ROW_DATE);
            int idColROW_STR_TIME = cursorExport.getColumnIndex(DatabaseHandler.ROW_STR_TIME);
            int idColROW_DATETIME = cursorExport.getColumnIndex(DatabaseHandler.ROW_DATETIME);
            int idColROW_LOG_TIME = cursorExport.getColumnIndex(DatabaseHandler.ROW_LOG_TIME);
            int idColROW_REG_NO = cursorExport.getColumnIndex(DatabaseHandler.ROW_REG_NO);
            int idColROW_AIRPLANE_TYPE = cursorExport.getColumnIndex(DatabaseHandler.ROW_AIRPLANE_TYPE);
            int idColROW_DAY_NIGHT = cursorExport.getColumnIndex(DatabaseHandler.ROW_DAY_NIGHT);
            int idColROW_IFR_VFR = cursorExport.getColumnIndex(DatabaseHandler.ROW_IFR_VFR);
            int idColROW_FLIGHT_TYPE = cursorExport.getColumnIndex(DatabaseHandler.ROW_FLIGHT_TYPE);
            int idColROW_DESCRIPTION = cursorExport.getColumnIndex(DatabaseHandler.ROW_DESCRIPTION);
            do {
                Log.i(TAG, "idColROW_ID: " + cursorExport.getString(idColROW_ID));
                Log.i(TAG, "idColROW_DATE: " + cursorExport.getString(idColROW_DATE));
                Log.i(TAG, "idColROW_STR_TIME: " + cursorExport.getString(idColROW_STR_TIME));
                Log.i(TAG, "idColROW_DATETIME: " + cursorExport.getString(idColROW_DATETIME));
                Log.i(TAG, "idColROW_LOG_TIME: " + cursorExport.getString(idColROW_LOG_TIME));
                Log.i(TAG, "idColROW_REG_NO: " + cursorExport.getString(idColROW_REG_NO));
                Log.i(TAG, "idColROW_AIRPLANE_TYPE: " + cursorExport.getString(idColROW_AIRPLANE_TYPE));
                Log.i(TAG, "idColROW_DAY_NIGHT: " + cursorExport.getString(idColROW_DAY_NIGHT));
                Log.i(TAG, "idColROW_IFR_VFR: " + cursorExport.getString(idColROW_IFR_VFR));
                Log.i(TAG, "idColROW_FLIGHT_TYPE: " + cursorExport.getString(idColROW_FLIGHT_TYPE));
                Log.i(TAG, "idColROW_DESCRIPTION: " + cursorExport.getString(idColROW_DESCRIPTION));

                // Generate column headings
                row = sheet_main.createRow(cursorExport.getInt(idColROW_ID));
                c = row.createCell(0);
                c.setCellValue(cursorExport.getString(idColROW_DATE));
                c = row.createCell(1);
                int logTime = cursorExport.getInt(idColROW_LOG_TIME);
                int Hours = logTime / 60;
                int Minutes = logTime % 60;
                c.setCellValue(String.valueOf(pad(Hours) + ":" + pad(Minutes)));
                c = row.createCell(2);
                c.setCellValue(cursorExport.getString(idColROW_AIRPLANE_TYPE));
                c = row.createCell(3);
                c.setCellValue(cursorExport.getString(idColROW_REG_NO));
                c = row.createCell(4);
                c.setCellValue(cursorExport.getString(idColROW_DAY_NIGHT));
                c = row.createCell(5);
                c.setCellValue(cursorExport.getString(idColROW_IFR_VFR));
                c = row.createCell(6);
                c.setCellValue(cursorExport.getString(idColROW_FLIGHT_TYPE));
                c = row.createCell(7);
                c.setCellValue(cursorExport.getString(idColROW_DESCRIPTION));
            } while (cursorExport.moveToNext());
        } else {
            Log.i(TAG, "0 rows");
        }
        cursorExport.close();

        sheet_main.setColumnWidth(0, (15 * 200));
        sheet_main.setColumnWidth(1, (15 * 150));
        sheet_main.setColumnWidth(2, (15 * 150));
        sheet_main.setColumnWidth(3, (15 * 150));
        sheet_main.setColumnWidth(4, (15 * 250));
        sheet_main.setColumnWidth(5, (15 * 300));
        sheet_main.setColumnWidth(6, (15 * 200));
        sheet_main.setColumnWidth(7, (15 * 500));*/

		/*//Create second sheet airplane types
		//New Sheet
		Sheet sheet_types = null;
		sheet_types = wb.createSheet(LOG_SHEET_TYPE);
		//create base row
		row = sheet_types.createRow(0);
		c = row.createCell(0);
		c.setCellValue(XLS_CELL_TYPES);

		cursorTypes = db.getAllTypes();
		if (cursorTypes.moveToFirst()) {
			int idColROW_ID = cursorExport.getColumnIndex(DatabaseHandler.ROW_ID);
			int idColROW_AIRPLANE_TYPE = cursorTypes.getColumnIndex(DatabaseHandler.ROW_AIRPLANE_TYPE);
			do {
				Log.i(TAG, "idColROW_ID: " + cursorExport.getString(idColROW_ID));
				Log.i(TAG, "idColROW_AIRPLANE_TYPE: " + cursorTypes.getString(idColROW_AIRPLANE_TYPE));
				// Generate column headings
				row = sheet_main.createRow(cursorExport.getInt(idColROW_ID));
				c = row.createCell(0);
				c.setCellValue(cursorTypes.getString(idColROW_AIRPLANE_TYPE));
			} while (cursorTypes.moveToNext());
		} else {
			Log.i(TAG, "0 rows");
		}
		cursorTypes.close();*/

        // Create a path where we will place our List of objects on external storage
       /* File file = new File(context.getExternalFilesDir(null), fileName);
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.i(TAG, "Writing file" + file);
            Toast.makeText(MainActivity.this, getString(R.string.str_file_saved)+" "+file, Toast.LENGTH_SHORT).show();
            success = true;
        } catch (IOException e) {
            Log.i(TAG, "Error writing " + file, e);
        } catch (Exception e) {
            Log.i(TAG, "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
                Log.i(TAG, "Exception " + ex.toString());
            }
        }
        return success;
    }

    private void readExcelFile(Context context, String filename) {
        boolean hasType=false;
        boolean checked = false;

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.i(TAG, "Storage not available or read only");
            return;
        }

        try {
            // Creating Input Stream
            File file = new File(context.getExternalFilesDir(null), filename);
            FileInputStream myInput = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);
            /** We now need something to iterate through the cells.**/
           /* Iterator rowIter = mySheet.rowIterator();
            int rowCnt = 0;
            db.deleteItems();
            while (rowIter.hasNext()) {
                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator cellIter = myRow.cellIterator();
                Log.i(TAG, "rowIter "+rowCnt);
                int cellCnt = 0;
                while (cellIter.hasNext()) {
                    HSSFCell myCell = (HSSFCell) cellIter.next();
                    Log.i(TAG, "Cell: "+cellCnt);
                    Log.i(TAG, "Cell Value: " + myCell.toString());
                    if (rowCnt>0){
                        Log.i(TAG, "Cell: "+cellCnt);
                        switch (cellCnt) {
                            case 0:
                                try {
                                    strDate = myCell.toString();
                                } catch (Exception e) {
                                    strDate = getCurrentDate();
                                    Log.i(TAG, "readExcelFile Exception strDate "+e.toString());
                                }
                                Log.i(TAG, "strDate "+strDate);
                                break;
                            case 1:
                                try {
                                    strTime = myCell.toString();
                                } catch (Exception e) {
                                    strTime = "00:00";
                                    Log.i(TAG, "readExcelFile Exception strTime"+e.toString());
                                }
                                Log.i(TAG, "strTime "+strTime);
                                break;
                            case 2:
                                try {
                                    airplane_type = myCell.toString();
                                    cursorTypes = db.getAllTypes();
                                    if (cursorTypes.moveToFirst()) {
                                        int idColROW_AIRPLANE_TYPE = cursorTypes.getColumnIndex(DatabaseHandler.ROW_AIRPLANE_TYPE);
                                        do {
                                            Log.i(TAG, "idColROW_AIRPLANE_TYPE: " + cursorTypes.getString(idColROW_AIRPLANE_TYPE));
                                            hasType = airplane_type.equals(cursorTypes.getString(idColROW_AIRPLANE_TYPE));
                                        } while (cursorTypes.moveToNext());
                                        Log.i(TAG, "hasType " + hasType);

                                        if (hasType) {
                                            checked=true;
                                        }else{
                                            Log.i(TAG, "checked " + checked);
                                            if (!checked) {
                                                db.insertType(airplane_type);
                                            }
                                        }
                                    } else {
                                        Log.i(TAG, "0 rows");
                                    }
                                    cursorTypes.close();
                                } catch (Exception e) {
                                    airplane_type = "";
                                    Log.i(TAG, "readExcelFile Exception airplane_type"+e.toString());
                                }
                                Log.i(TAG, "airplane_type "+airplane_type);
                                break;
                            case 3:
                                try {
                                    reg_no = myCell.toString();
                                } catch (Exception e) {
                                    reg_no = "";
                                    Log.i(TAG, "readExcelFile Exception reg_no"+e.toString());
                                }
                                Log.i(TAG, "reg_no "+reg_no);
                                break;
                            case 4:
                                try {
                                    day_night = Integer.parseInt(myCell.toString());
                                } catch (Exception e) {
                                    day_night = 0;
                                    Log.i(TAG, "readExcelFile Exception day_night "+e.toString());
                                }
                                Log.i(TAG, "day_night "+day_night);
                                break;
                            case 5:
                                try {
                                    ifr_vfr = Integer.parseInt(myCell.toString());
                                } catch (Exception e) {
                                    ifr_vfr = 0;
                                    Log.i(TAG, "readExcelFile Exception ifr_vfr "+e.toString());
                                }
                                Log.i(TAG, "ifr_vfr "+ifr_vfr);
                                break;
                            case 6:
                                try {
                                    flight_type = Integer.parseInt(myCell.toString());
                                } catch (Exception e) {
                                    flight_type = 0;
                                    Log.i(TAG, "readExcelFile Exception flight_type "+e.toString());
                                }
                                Log.i(TAG, "flight_type "+flight_type);
                                break;
                            case 7:
                                try {
                                    strDesc = myCell.toString();
                                } catch (Exception e) {
                                    strDesc = "";
                                    Log.i(TAG, "readExcelFile Exception strDesc"+e.toString());
                                }
                                Log.i(TAG, "strDesc "+strDesc);
                                try {
                                    logTime = convertStringToTime(strTime);
                                    mDateTime = convertTimeStringToLong(strDate);
                                    Log.i(TAG, "strDesc: " + strDesc);
                                    Log.i(TAG, "strDate: " + strDate);
                                    Log.i(TAG, "strTime: " + strTime);
                                    Log.i(TAG, "logTime: " + logTime);
                                    Log.i(TAG, "reg_no: " + reg_no);
                                    Log.i(TAG, "airplane_type: " + airplane_type);
                                    Log.i(TAG, "day_night: " + day_night);
                                    Log.i(TAG, "ifr_vfr: " + ifr_vfr);
                                    Log.i(TAG, "flight_type: " + flight_type);
                                    db.insertItem(strDate, mDateTime, logTime, strTime, reg_no, airplane_type, day_night, ifr_vfr, flight_type, strDesc);
                                } catch (Exception e) {
                                    Log.i(TAG, "readExcelFile Exception"+e.toString());
                                }
                                break;
                        }//switch (cellCnt)
                    }//if (rowCnt>0)
                    cellCnt++;
                }//cellIter.hasNext()
                rowCnt++;
            }//while (rowIter.hasNext())
            cursorExport.requery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//readFile*/



    /*private void showImportAlert() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.str_import_attention));
		alert.setMessage(getString(R.string.str_import_massage));
		alert.setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		alert.setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//readExcelFile(getBaseContext(), EXEL_FILE_NAME);
				scAdapter.notifyDataSetChanged();
				cursor.requery();// обновляем курсор
				displayTotalTime();
				Toast.makeText(MainActivity.this, getString(R.string.str_import_success), Toast.LENGTH_SHORT).show();
			}
		});
		alert.show();
	}*/



    /*private void showExportAlert() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.str_export_attention));
		alert.setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		alert.setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveExcelFile(getBaseContext(), EXEL_FILE_NAME);
				Toast.makeText(MainActivity.this, getString(R.string.str_export_success), Toast.LENGTH_SHORT).show();
			}
		});
		alert.show();
	}*/



    /*	private void displayTotalTime() {
		int tmpLogTime = 0;
		cursorTime = db.getAllItems();
		if (cursorTime.moveToFirst()) {
			try {
				do {
					int idColLogTime = cursorTime.getColumnIndex(DatabaseHandler.ROW_LOG_TIME);
					tmpLogTime += cursorTime.getLong(idColLogTime);
				} while (cursorTime.moveToNext());
			} catch (Exception e) {
				Log.i("LOG_TAG", e.toString());
			}
		} else {
			Log.i(TAG, "cursorTime end");
		}
		cursorTime.close();
		totalH = tmpLogTime / 60;
		totalMin = tmpLogTime % 60;
		tvTotalString = getString(R.string.str_totaltime) + " " + pad(totalH) + ":" + pad(totalMin);
		tvTotalTime.setText(tvTotalString);
	}*/


   /* static final String ALTER_TABLE_10 =
            "BEGIN TRANSACTION; " +
                    "CREATE TEMPORARY TABLE " + MAIN_TABLE + "_backup ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY, "
                    + COLUMN_DATETIME + " TIMESTAMP,"
                    + COLUMN_LOG_TIME + " INTEGER,"
                    + COLUMN_REG_NO + " TEXT,"
                    + COLUMN_AIRPLANE_TYPE + " INTEGER,"
                    + COLUMN_DAY_NIGHT + " INTEGER,"
                    + COLUMN_IFR_VFR + " INTEGER,"
                    + COLUMN_FLIGHT_TYPE + " INTEGER,"
                    + COLUMN_DESCRIPTION + " TEXT"
                    + ");" +
                    "INSERT INTO " + MAIN_TABLE + "_backup SELECT " + COLUMN_ID + ", " + COLUMN_DATETIME + ", " + COLUMN_LOG_TIME + ", " + COLUMN_REG_NO + ", " + COLUMN_AIRPLANE_TYPE + ", " + COLUMN_DAY_NIGHT + ", " + COLUMN_IFR_VFR + ", " + COLUMN_FLIGHT_TYPE + ", " + COLUMN_DESCRIPTION + " FROM " + MAIN_TABLE + ";" +
                    "DROP TABLE " + MAIN_TABLE + ";" +
                    "CREATE TABLE " + MAIN_TABLE + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY, "
                    + COLUMN_DATETIME + " TIMESTAMP,"
                    + COLUMN_LOG_TIME + " INTEGER,"
                    + COLUMN_REG_NO + " TEXT,"
                    + COLUMN_AIRPLANE_TYPE + " INTEGER,"
                    + COLUMN_DAY_NIGHT + " INTEGER,"
                    + COLUMN_IFR_VFR + " INTEGER,"
                    + COLUMN_FLIGHT_TYPE + " INTEGER,"
                    + COLUMN_DESCRIPTION + " TEXT"
                    + ");" +
                    "INSERT INTO " + MAIN_TABLE + " SELECT " + COLUMN_ID + ", " + COLUMN_DATETIME + ", " + COLUMN_LOG_TIME + ", " + COLUMN_REG_NO + ", " + COLUMN_AIRPLANE_TYPE + ", " + COLUMN_DAY_NIGHT + ", " + COLUMN_IFR_VFR + ", " + COLUMN_FLIGHT_TYPE + ", " + COLUMN_DESCRIPTION + " FROM " + MAIN_TABLE + "_backup;" +
                    "DROP TABLE " + MAIN_TABLE + "_backup;"
                    + "CREATE TEMPORARY TABLE "+TYPE_TABLE+"_backup ("
                    + COLUMN_TYPE_ID + " INTEGER PRIMARY KEY, "
                    + COLUMN_AIRPLANE_TYPE_TITLE + " TEXT"
                    + ");"+
                    "INSERT INTO "+TYPE_TABLE+"_backup SELECT " + COLUMN_TYPE_ID + ", airplane_type FROM "+TYPE_TABLE+";"
                    + "DROP TABLE "+TYPE_TABLE+";" +
                    "CREATE TABLE "+TYPE_TABLE+" ("
                    + COLUMN_TYPE_ID + " INTEGER PRIMARY KEY, "
                    + COLUMN_AIRPLANE_TYPE_TITLE + " TEXT"
                    + ");"+
                    "INSERT INTO "+TYPE_TABLE+" SELECT " + COLUMN_TYPE_ID + ", airplane_type FROM "+TYPE_TABLE+"_backup;" +
                    "DROP TABLE "+TYPE_TABLE+"_backup;"
                    +"COMMIT;";*/

    /*private void removeTheDuplicates(List<String> myList) {
        for (ListIterator<String> iterator = myList.listIterator(); iterator.hasNext(); ) {
            String customer = iterator.next();
            if (Collections.frequency(myList, customer) > 1) {
                iterator.remove();
            }
        }
        Log.i(TAG, "myList.toString() " + Arrays.toString(myList.toArray()));
    }*/

}
