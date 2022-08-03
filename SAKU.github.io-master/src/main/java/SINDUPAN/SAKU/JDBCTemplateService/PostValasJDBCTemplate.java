package SINDUPAN.SAKU.JDBCTemplateService;

import SINDUPAN.SAKU.DAO.GetListTRXDAO;
import SINDUPAN.SAKU.DAO.PostValasDAO;
import SINDUPAN.SAKU.Mapper.*;
import SINDUPAN.SAKU.Model.GetListTRXModel;
import SINDUPAN.SAKU.Model.GetListTransaksiModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;


@Component
public class PostValasJDBCTemplate implements PostValasDAO {
    @Autowired
    public JdbcTemplate jdbcTemplateObject;
    @Autowired
    public List<GetListTransaksiModel> listDataTRX()
    {
        String SQL = "SELECT * FROM trx_master";
        List <GetListTransaksiModel> DatadetailsTRXModels = jdbcTemplateObject.query(SQL, new GetListTrxMapper());
        return DatadetailsTRXModels;

    }

    public List<GetListTRXModel>listjurnal() // no parameter
    {
        String SQL = "select * from vw_jurnal";
        return jdbcTemplateObject.query(SQL, new GetListDtlJurnalMapper());
    }
//    public List<GetListTRXModel>listledger() //no parameter
//    {
//        String SQL = "call sp_getledger()";
//        return jdbcTemplateObject.query(SQL, new GetListLedgerMapper());
//    }

    public List<GetListTRXModel>listledger(String nocoa)
    {
        String SQL = "call sp_getledger(?)";
        return jdbcTemplateObject.query(SQL, new GetListLedgerMapper(), new Object[]{nocoa});
    }
    public List<GetListTRXModel>getcoaforledger()
    {
        String SQL = "select \n" +
                "dc.NAMA_COA as 'NAMA_COA', \n" +
                "td.NO_COA_DBT as 'NO_COA'\n" +
                "from trx_debit td\n" +
                "join daftar_coa dc on td.NO_COA_DBT = dc.NO_COA \n" +
                "union \n" +
                "select \n" +
                "dc.NAMA_COA as 'NAMA_COA', \n" +
                "tk.NO_COA_KDT as 'NO_COA'\n" +
                "from trx_kredit tk \n" +
                "join daftar_coa dc on tk.NO_COA_KDT = dc.NO_COA ";
        List <GetListTRXModel> DatadetailsTRXModels = jdbcTemplateObject.query(SQL, new GetListCOALedgerMapper());
        return DatadetailsTRXModels;
    }


    public List<GetListTRXModel> listdetailtrxdbt(String id_trx)
    {
//        String SQL = "SELECT * FROM trx_debit where NO_TRXDBT=?";
        String SQL = "select \n" +
                "td.NO_TRXDBT ,\n" +
                "td.NO_COA_DBT,\n" +
                "dc.NAMA_COA  as NAMA_COA_DBT,\n" +
                "rmu.KET as MATA_UANG_DBT,\n" +
                "td.INVOICE_DBT,\n" +
                "td.NOMINALTRXDBT,\n" +
                "td.KTRG_DBT \n" +
                "from trx_debit td\n" +
                "join daftar_coa dc on td.NO_COA_DBT = dc.NO_COA  \n" +
                "join ref_mata_uang rmu on td.MATA_UANG_DBT = rmu.Id  \n" +
                "where NO_TRXDBT = ?";
        try {
            List<GetListTRXModel> datadetailstrx = jdbcTemplateObject.query(SQL, new GetListTrxDetailDBTMapper(), new Object[]{id_trx});
            return datadetailstrx;
        }
        catch  (EmptyResultDataAccessException e) {
            return null;
        }
    }
    public List<GetListTRXModel> listdetailtrxkdt(String id_trx)
    {
//        String SQL = "SELECT * FROM trx_kredit where NO_TRXKDT=?";
        String SQL ="select \n" +
                "td.NO_TRXKDT ,\n" +
                "td.NO_COA_KDT,\n" +
                "dc.NAMA_COA  as NAMA_COA_KDT,\n" +
                "rmu.KET as MATA_UANG_KDT,\n" +
                "td.INVOICE_KDT,\n" +
                "td.NOMINALTRXKDT,\n" +
                "td.KTRG_KDT \n" +
                "from trx_kredit td\n" +
                "join daftar_coa dc on td.NO_COA_KDT = dc.NO_COA  \n" +
                "join ref_mata_uang rmu on td.MATA_UANG_KDT = rmu.Id  \n" +
                "where NO_TRXKDT = ?";
        try {
            return  jdbcTemplateObject.query(SQL, new GetListTrxDetailKDTMapper(), new Object[]{id_trx});
        }
        catch  (EmptyResultDataAccessException e) {
            return null;
        }
    }
    public int checkingdata()
    {
        String SQL = "  \n" +
                "select EXISTS (SELECT * FROM currencyvalue WHERE date(UPDATE_DATE) = curdate())\n";
        int result = jdbcTemplateObject.queryForObject(SQL, int.class);
        return result;
    }

    public void insertvaluetodatabase(String KODE, Double Rate)
    {
        LocalDateTime getdate = LocalDateTime.now();
        String SQL = "insert into currencyvalue\n" +
                "(KD_MATA_UANG, RATE, UPDATE_DATE, UPDATE_BY)\n" +
                "values \n" +
                "(?, ?, ?, ?)";
        jdbcTemplateObject.update(SQL,KODE, Rate, LocalDateTime.now(), null );
    }
    public void gnrttrxnmbr()
    {
        String SQL = "insert into trx_master (TGL_TRX) values(curdate())";
        jdbcTemplateObject.update(SQL);
    }
    public String gettrxnbr ()
    {

        String SQL ="select MAX(NO_TRX) from trx_master tm";
        return jdbcTemplateObject.queryForObject(SQL, String.class );
    }


    public void insertdbt(String NO_TRX, String NO_COA, String MATA_UANG,  String INVOICE, String NOMINAL_DBT, String KTRG_DBT, String EKIVRPDBT)
    {
        String SQL = "call sp_addtrxdbt(?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplateObject.update(SQL,
                NO_TRX,
                NO_COA,
                MATA_UANG,
                INVOICE,
                NOMINAL_DBT,
                KTRG_DBT,
                EKIVRPDBT
                );
    }
    public void insertkdt(String NO_TRX, String NO_COA, String MATA_UANG,  String INVOICE, String NOMINAL_DBT, String KTRG_DBT, String EKIVRPKDT)
    {
        String SQL = "call sp_addtrxkdt(?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplateObject.update(SQL,
                NO_TRX,
                NO_COA,
                MATA_UANG,
                INVOICE,
                NOMINAL_DBT,
                KTRG_DBT,
                EKIVRPKDT
        );
    }

//    @Autowired
//    public DataSource dataSource;
//    @Autowired
//    public void setDataSource(DataSource ds) {
//        dataSource = ds;
//        jdbcTemplateObject = new JdbcTemplate(dataSource);
//
//    }

//    buat test
//    @Autowired
//    public List<MasterModel> listMaster()
//    {
//        String SQL = "select NO_COA as 'NO_COA1', NAMA_COA, TANGGAL, KET from master_akutansi";
//        List <MasterModel> MasterModels = jdbcTemplateObject.query(SQL, new MasterMapper());
//        return MasterModels;
//
//    }








}
