Attribute VB_Name = "standard"
Sub openBook()

    Dim OpenFileName As String
    Dim dirName As String
    Dim pathName As String
    Dim FileName As String
    
    
    dirName = "C"
    pathName = "C:\MyDoc\plan"
    
    ChDrive dirName
    ChDir pathName
    OpenFileName = Application.GetOpenFilename("Microsoft Excel�u�b�N,*.xlsx")
    
    'MsgBox OpenFileName
    
    If OpenFileName <> "False" Then
    
        
        Dim xlBook
        Set xlBook = Workbooks.Open(OpenFileName)
               
        
        xlBook.Worksheets("price").Activate          '���[�N�V�[�g���A�N�e�B�u�ɂ��� ���P
        Range("A1:Q22").Copy                         '�R�s�[���� ���Q
        ActiveSheet.Paste Destination:=ThisWorkbook.Worksheets("price").Range("A1:Q22") '�\��t���� ���R�A�S
        
        
        
        Application.DisplayAlerts = Flse
        xlBook.Close
        Application.DisplayAlerts = True
        
        ThisWorkbook.Worksheets("plan").Activate
        
        'FileName = Dir(OpenFileName)
        ThisWorkbook.Worksheets("menu").Range("A7").MergeArea = Dir(OpenFileName)
        
    
    End If


End Sub

Sub savePlan()


    Dim SaveDir As String
    Dim pathName As String
    
    
    'dirName = "C"
    pathName = ThisWorkbook.Worksheets("menu").Range("A12").Text

    MsgBox pathName

    If Dir(pathName, vbDirectory) = "" Then
        MkDir pathName
    End If
    
    
    Dim printSheet As String
    Dim customerName As String

    printSheet = "plan"
    customerName = ThisWorkbook.Worksheets("plan").Range("B1")
    
    MsgBox customerName

    Call printPDF(printSheet, customerName)


End Sub


' printSheet��PDF�������V�[�g���AcustomerName�͍쐬����PDF�̃t�@�C�����Ɏg������
Sub printPDF(ByVal printSheet As String, ByVal customerName As String)

    
    pdfName = Format(Now(), "yyyymmdd_hhmm") & "_" & customerName
    
    MsgBox pdfName
    
    Dim pathName As String
    
    pathName = ThisWorkbook.Worksheets("menu").Range("A12").Text
    
    
    'ChDir "C:\MyDoc\plan" '�J�����g�t�H���_�̎w��B�����Ŏw�肵���t�H���_��PDF���ۑ�����܂��B
    
    ChDir pathName
    
    fileSaveName = Application.GetSaveAsFilename(pdfName, _
    fileFilter:="PDF Files (*.pdf), *.pdf")
    
    If fileSaveName <> False Then
     ' ����ݒ�̎w��
     With Sheets(printSheet).PageSetup
                    .Orientation = xlPortrait ' �c�������
                   '.Orientation = xlLandscape '���������
                    .FitToPagesWide = 1 '����������p���Ƀt�B�b�g������
                    .FitToPagesTall = 1 '�c��������p���Ƀt�B�b�g������
                    .Zoom = False ' �g�債�Ȃ�
                    .PrintArea = "B$1:$H$24" '����͈͂̎w��
                    '.PageSize = xlPaperA4 '����p���T�C�Y�̎w��
                 
     End With
    
      Sheets(printSheet).ExportAsFixedFormat Type:=xlTypePDF, _
        FileName:=fileSaveName, _
        Quality:=xlQualityStandard, _
        IncludeDocProperties:=True, _
        IgnorePrintAreas:=False, _
        OpenAfterPublish:=True
        
    End If
    'MsgBox "PDF File Saved to" & " " & fileSaveName
    
End Sub








