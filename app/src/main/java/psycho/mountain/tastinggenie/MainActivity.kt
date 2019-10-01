package psycho.mountain.tastinggenie

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_test_fragment.*

class MainActivity : AppCompatActivity(), TestFragment.FragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (savedInstanceState == null){
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            //fragmentTransaction.addToBackStack(null)

            fragmentTransaction.replace(R.id.container, TestFragment.newInstance())
            fragmentTransaction.commit()
        }
    }

    override fun onClickButton() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // 戻り先がnull菜のではなく，バックスタックに遷移を記録する（戻せるようにする）という意味
        fragmentTransaction.addToBackStack(null)

        fragmentTransaction.replace(R.id.container, TestFragment2.newInstance())
        fragmentTransaction.commit()
    }




}
