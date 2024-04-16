import pandas as pd


def readLossValues(path):
    column_names = ["epoch_no", "train_loss", "val_loss", "epoch_time"]
    df = pd.DataFrame(columns=column_names)
    count = 0
    with open(path) as fp:
        Lines = fp.readlines()
        for line in Lines:
            if "epoch:".lower() in line.lower():
                infors = line.lower().strip().split(",")
                epoch_no = int(infors[0][infors[0].index('epoch:')+len('epoch:'):])
                if epoch_no % 50 == 0:
                    train_loss = float(infors[1][infors[1].index(
                        'loss:')+len('loss:'):infors[1].index('(')])
                    val_loss = float(infors[2][infors[2].index(
                        'val:')+len('val:'):infors[2].index('(')])
                    epoch_time = float(
                        infors[3][infors[3].index('epoch:')+len('epoch:'):])
                    df = df.append({'epoch_no': epoch_no, 'train_loss': train_loss,
                                   'val_loss': val_loss, 'epoch_time': epoch_time}, ignore_index=True)
                # print("{}:{}".format(epoch_no,loss))
                # print(infors[2][infors[2].index('val:')+len('val:'):infors[2].index('(')])
                # print(infors[3][infors[3].index('epoch:')+len('epoch:'):])
                # for s in infors:
                #     print(s)
    return df


if __name__ == "__main__":
    sage_log_file_path = "/home/gqxwolf/mydata/projectData/skylineGNN/logs/C9_NY_NONE_5K/SAGE/256_256_32_1000_1_train_checkpoint_SAGE_embedTrue_ConLossFalse_2GNN3FF_10k_train_samples_27092021_144616.log"
    sage_df = readLossValues(log_file_path.strip())
